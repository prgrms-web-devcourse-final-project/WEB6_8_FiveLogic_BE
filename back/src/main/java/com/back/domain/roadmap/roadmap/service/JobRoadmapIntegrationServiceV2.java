package com.back.domain.roadmap.roadmap.service;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.repository.JobRepository;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode.RoadmapType;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapNodeStatRepository;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.global.exception.ServiceException;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapIntegrationServiceV2 {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final JobRepository jobRepository;
    private final JobRoadmapRepository jobRoadmapRepository;
    private final TaskRepository taskRepository;
    private final JobRoadmapNodeStatRepository jobRoadmapNodeStatRepository;

    // --- 통합 알고리즘 상수 ---
    private final double BRANCH_THRESHOLD = 0.25;
    private final int MAX_DEPTH = 10;
    private final int MAX_CHILDREN = 4;
    private final int MAX_DEFERRED_RETRY = 3;

    // --- 품질 필터링 상수 ---
    private static final double MIN_STANDARDIZATION_RATE = 0.5; // 최소 표준화율 50%
    private static final double MIN_QUALITY_THRESHOLD = 0.4;    // 최소 품질 점수 40%
    private static final double QUALITY_NODE_COUNT_WEIGHT = 0.3; // 품질 점수: 노드 개수 가중치
    private static final double QUALITY_STANDARDIZATION_WEIGHT = 0.7; // 품질 점수: 표준화율 가중치

    // --- 부모 우선순위 점수(priorityScore) 가중치 ---
    private static final double W_TRANSITION_POPULARITY = 0.4; // 전체 멘토 대비 전이 빈도 (전체적인 인기)
    private static final double W_TRANSITION_STRENGTH = 0.3;   // 부모 노드 내에서의 전이 강도 (연결의 확실성)
    private static final double W_POSITION_SIMILARITY = 0.2;   // 부모-자식 노드 간 평균 위치 유사성
    private static final double W_MENTOR_COVERAGE = 0.1;       // 부모 노드의 신뢰도 (얼마나 많은 멘토가 언급했나)


    @Transactional
    public JobRoadmap integrateJobRoadmap(Long jobId) {
        // =================================================================
        // === 1. 데이터 준비 및 필터링 (Preparation & Filtering) ===
        // =================================================================
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ServiceException("404", "직업을 찾을 수 없습니다. id=" + jobId));

        jobRoadmapRepository.findByJob(job).ifPresent(existing -> {
            jobRoadmapRepository.delete(existing);
            log.info("기존 JobRoadmap 삭제: id={}", existing.getId());
        });

        List<MentorRoadmap> mentorRoadmaps = mentorRoadmapRepository.findAllByMentorJobIdWithNodes(jobId);

        List<MentorRoadmap> qualityFiltered = mentorRoadmaps.stream()
                .filter(mr -> mr.getNodes() != null && mr.getNodes().size() >= 3)
                .filter(mr -> calculateStandardizationRate(mr) >= MIN_STANDARDIZATION_RATE)
                .filter(mr -> calculateRoadmapQuality(mr) >= MIN_QUALITY_THRESHOLD)
                .toList();

        if (qualityFiltered.isEmpty()) {
            throw new ServiceException("404", "해당 직업에 대한 유효한 멘토 로드맵이 존재하지 않습니다. " +
                    "(최소 조건: 노드 3개 이상, 표준화율 " + (int)(MIN_STANDARDIZATION_RATE * 100) + "% 이상, 품질 점수 " + MIN_QUALITY_THRESHOLD + " 이상)");
        }

        log.info("멘토 로드맵 품질 필터링: 전체 {}개 → 유효 {}개 (노드 3개 이상, 표준화율 {}% 이상, 품질 점수 {} 이상)",
                mentorRoadmaps.size(), qualityFiltered.size(), (int)(MIN_STANDARDIZATION_RATE * 100), MIN_QUALITY_THRESHOLD);
        mentorRoadmaps = qualityFiltered;
        final int totalMentorCount = mentorRoadmaps.size();

        // =================================================================
        // === 2. 통계 집계 (Statistics Aggregation) ===
        // =================================================================
        Map<String, AggregatedNode> agg = new HashMap<>();
        Map<String, Map<String, Integer>> transitions = new HashMap<>();
        Map<String, Integer> rootCount = new HashMap<>();
        Map<String, Set<Long>> mentorAppearSet = new HashMap<>();
        Map<String, List<Integer>> positions = new HashMap<>();
        Map<String, List<String>> learningAdvices = new HashMap<>();
        Map<String, List<String>> recommendedResourcesList = new HashMap<>();
        Map<String, List<String>> learningGoalsList = new HashMap<>();
        Map<String, List<Integer>> difficulties = new HashMap<>();
        Map<String, List<Integer>> importances = new HashMap<>();
        Map<String, List<Integer>> estimatedHoursList = new HashMap<>();

        for (MentorRoadmap mr : mentorRoadmaps) {
            List<RoadmapNode> nodes = mr.getNodes().stream()
                    .sorted(Comparator.comparingInt(RoadmapNode::getStepOrder))
                    .toList();
            if (nodes.isEmpty()) continue;

            rootCount.merge(generateKey(nodes.get(0)), 1, Integer::sum);
            Long mentorId = mr.getMentor().getId();

            for (int i = 0; i < nodes.size(); i++) {
                RoadmapNode rn = nodes.get(i);
                String k = generateKey(rn);

                agg.computeIfAbsent(k, kk -> new AggregatedNode(rn.getTask(), rn.getTask() != null ? rn.getTask().getName() : rn.getTaskName())).count++;
                positions.computeIfAbsent(k, kk -> new ArrayList<>()).add(i + 1);
                mentorAppearSet.computeIfAbsent(k, kk -> new HashSet<>()).add(mentorId);

                // learningAdvice 모으기
                if (rn.getLearningAdvice() != null && !rn.getLearningAdvice().isBlank()) {
                    learningAdvices.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getLearningAdvice());
                }

                // recommendedResources 모으기
                if (rn.getRecommendedResources() != null && !rn.getRecommendedResources().isBlank()) {
                    recommendedResourcesList.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getRecommendedResources());
                }

                // learningGoals 모으기
                if (rn.getLearningGoals() != null && !rn.getLearningGoals().isBlank()) {
                    learningGoalsList.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getLearningGoals());
                }

                // difficulty 모으기
                if (rn.getDifficulty() != null) {
                    difficulties.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getDifficulty());
                }

                // importance 모으기
                if (rn.getImportance() != null) {
                    importances.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getImportance());
                }

                // estimatedHours 모으기
                if (rn.getEstimatedHours() != null) {
                    estimatedHoursList.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getEstimatedHours());
                }

                if (i < nodes.size() - 1) {
                    transitions.computeIfAbsent(k, kk -> new HashMap<>()).merge(generateKey(nodes.get(i + 1)), 1, Integer::sum);
                }
            }
        }

        // =================================================================
        // === 3. 루트 노드 선택 및 노드 인스턴스 생성 (Root Selection & Node Instantiation) ===
        // =================================================================
        String rootKey = rootCount.entrySet().stream()
                .max(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElseGet(() -> agg.entrySet().stream()
                        .max(Comparator.comparingInt(e -> e.getValue().count))
                        .map(Map.Entry::getKey)
                        .orElseThrow());
        log.info("선택된 rootKey={} (빈도={})", rootKey, rootCount.getOrDefault(rootKey, 0));

        Map<String, RoadmapNode> keyToNode = new HashMap<>();
        agg.forEach((key, a) -> {
            // 숫자 필드 평균 계산
            Double avgDifficulty = calculateAverage(difficulties.get(key));
            Double avgImportance = calculateAverage(importances.get(key));
            Integer avgEstimatedHours = calculateIntegerAverage(estimatedHoursList.get(key));

            RoadmapNode node = RoadmapNode.builder()
                    .taskName(a.displayName)
                    .learningAdvice(mergeTopDescriptions(learningAdvices.get(key)))
                    .recommendedResources(mergeTopDescriptions(recommendedResourcesList.get(key)))
                    .learningGoals(mergeTopDescriptions(learningGoalsList.get(key)))
                    .difficulty(avgDifficulty != null ? avgDifficulty.intValue() : null)
                    .importance(avgImportance != null ? avgImportance.intValue() : null)
                    .estimatedHours(avgEstimatedHours)
                    .task(a.task)
                    .roadmapId(0L)
                    .roadmapType(RoadmapType.JOB)
                    .build();
            keyToNode.put(key, node);
        });

        // =================================================================
        // === 4. 부모 후보군 평가 (Parent Candidacy Evaluation) ===
        // =================================================================
        Map<String, List<String>> chosenChildren = new HashMap<>();
        Map<String, List<ParentCandidate>> childToParentCandidates = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> e : transitions.entrySet()) {
            String parentKey = e.getKey();
            Map<String, Integer> childTransitions = e.getValue();
            int parentTotalTransitions = childTransitions.values().stream().mapToInt(Integer::intValue).sum();

            List<Map.Entry<String, Integer>> sortedChildren = childTransitions.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(MAX_CHILDREN)
                    .toList();

            List<String> chosen = new ArrayList<>();
            for (int i = 0; i < sortedChildren.size(); i++) {
                Map.Entry<String, Integer> ce = sortedChildren.get(i);
                String childKey = ce.getKey();
                int transitionCount = ce.getValue();

                if (i == 0) { // 가장 빈번한 자식은 항상 선택
                    chosen.add(childKey);
                } else {
                    double ratio = (double) transitionCount / agg.get(parentKey).count;
                    if (ratio >= BRANCH_THRESHOLD) {
                        chosen.add(childKey);
                    }
                }

                // --- 정교화된 priorityScore 계산 ---
                double transitionPopularity = (double) transitionCount / totalMentorCount;
                double transitionStrength = (double) transitionCount / parentTotalTransitions;

                double avgParentPos = positions.getOrDefault(parentKey, Collections.emptyList()).stream().mapToInt(Integer::intValue).average().orElse(99.0);
                double avgChildPos = positions.getOrDefault(childKey, Collections.emptyList()).stream().mapToInt(Integer::intValue).average().orElse(99.0);
                double positionSimilarity = 1.0 / (1.0 + Math.abs(avgParentPos - avgChildPos));

                double mentorCoverage = (double) mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size() / totalMentorCount;

                double priorityScore =
                        W_TRANSITION_POPULARITY * transitionPopularity +
                        W_TRANSITION_STRENGTH * transitionStrength +
                        W_POSITION_SIMILARITY * positionSimilarity +
                        W_MENTOR_COVERAGE * mentorCoverage;

                childToParentCandidates.computeIfAbsent(childKey, k -> new ArrayList<>()) 
                        .add(new ParentCandidate(parentKey, transitionCount, priorityScore));
            }
            if (!chosen.isEmpty()) chosenChildren.put(parentKey, chosen);
        }

        Map<String, String> childToBestParent = new HashMap<>();
        childToParentCandidates.forEach((child, candidates) -> {
            candidates.sort(Comparator.comparingDouble(ParentCandidate::getPriorityScore).reversed()
                    .thenComparing(ParentCandidate::getParentKey));
            childToBestParent.put(child, candidates.get(0).parentKey);
        });

        // =================================================================
        // === 5. 메인 트리 구성 (Main Tree Construction via BFS) ===
        // =================================================================
        Queue<String> q = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, List<AlternativeParentInfo>> skippedParents = new HashMap<>();
        Queue<DeferredChild> deferredQueue = new ArrayDeque<>();

        if (keyToNode.containsKey(rootKey)) {
            visited.add(rootKey);
            q.add(rootKey);
        }

        while (!q.isEmpty()) {
            String pk = q.poll();
            RoadmapNode parentNode = keyToNode.get(pk);
            List<String> childs = chosenChildren.getOrDefault(pk, Collections.emptyList());
            int order = 1;
            for (String ck : childs) {
                if (visited.contains(ck)) {
                    recordAsAlternative(pk, ck, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    continue;
                }

                RoadmapNode childNode = keyToNode.get(ck);
                if (childNode == null) continue;

                String bestParent = childToBestParent.get(ck);
                if (bestParent != null && !bestParent.equals(pk)) {
                    if (!visited.contains(bestParent)) {
                        deferredQueue.add(new DeferredChild(pk, ck, MAX_DEFERRED_RETRY));
                    } else {
                        recordAsAlternative(pk, ck, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    }
                    continue;
                }

                if (parentNode.getLevel() + 1 >= MAX_DEPTH) {
                    log.warn("MAX_DEPTH({})" + " 초과로 노드 추가 중단: parent={}, child={}", MAX_DEPTH, pk, ck);
                    recordAsAlternative(pk, ck, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    continue;
                }

                visited.add(ck);
                parentNode.addChild(childNode);
                childNode.assignOrderInSiblings(order++);
                q.add(ck);
            }
        }

        // --- 2차: Deferred 재시도 ---
        int deferredProcessed = 0;
        while (!deferredQueue.isEmpty()) {
            DeferredChild dc = deferredQueue.poll();
            if (visited.contains(dc.childKey)) continue;

            String bestParent = childToBestParent.get(dc.childKey);
            if (bestParent != null && visited.contains(bestParent)) {
                RoadmapNode bestParentNode = keyToNode.get(bestParent);
                RoadmapNode childNode = keyToNode.get(dc.childKey);

                if (bestParentNode != null && childNode != null && bestParentNode.getLevel() + 1 < MAX_DEPTH) {
                    visited.add(dc.childKey);
                    bestParentNode.addChild(childNode);
                    childNode.assignOrderInSiblings(bestParentNode.getChildren().size());
                    deferredProcessed++;
                }
            } else if (dc.retryCount > 0) {
                deferredQueue.add(new DeferredChild(dc.parentKey, dc.childKey, dc.retryCount - 1));
            }
        }
        log.info("Deferred 재시도 완료: {}개 노드 연결", deferredProcessed);

        // =================================================================
        // === 6. 메인 루트 설정 및 고아 노드 로그 기록 ===
        // =================================================================
        RoadmapNode mainRoot = keyToNode.get(rootKey);
        if (mainRoot != null) {
            mainRoot.initializeAsRoot();
        }

        // 고아 노드(visited되지 않은 노드) 로그 기록
        long orphanCount = keyToNode.values().stream()
                .filter(n -> !visited.contains(generateKey(n)))
                .count();

        if (orphanCount > 0) {
            log.info("=== 제외된 고아 노드 ===");
            keyToNode.entrySet().stream()
                    .filter(e -> !visited.contains(e.getKey()))
                    .forEach(e -> {
                        String key = e.getKey();
                        AggregatedNode aggNode = agg.get(key);
                        int count = aggNode != null ? aggNode.count : 0;
                        int mentorCount = mentorAppearSet.getOrDefault(key, Collections.emptySet()).size();
                        log.info("  - 키: {}, 이름: {}, 출현빈도: {}회, 멘토수: {}명",
                                key, e.getValue().getTaskName(), count, mentorCount);
                    });
            log.info("총 {}개의 저빈도 노드가 메인 트리에서 제외되었습니다.", orphanCount);
        }

        // =================================================================
        // === 7. 최종 저장 (Finalization & Persistence) ===
        // =================================================================
        JobRoadmap jobRoadmap = jobRoadmapRepository.save(JobRoadmap.builder().job(job).build());
        Long roadmapId = jobRoadmap.getId();

        List<RoadmapNode> allNodes = keyToNode.values().stream()
                .filter(n -> visited.contains(generateKey(n)))
                .peek(n -> n.assignToRoadmap(roadmapId, RoadmapType.JOB))
                .toList();
        
        jobRoadmap.getNodes().addAll(allNodes);
        JobRoadmap saved = jobRoadmapRepository.save(jobRoadmap);

        List<JobRoadmapNodeStat> stats = new ArrayList<>();
        for (RoadmapNode persisted : saved.getNodes()) {
            String k = generateKey(persisted);
            AggregatedNode a = agg.get(k);

            // 해당 키를 선택한 멘토 수
            int mentorCount = mentorAppearSet.getOrDefault(k, Collections.emptySet()).size();

            // 평균 단계 위치
            List<Integer> posList = positions.getOrDefault(k, Collections.emptyList());
            Double avgPos = posList.isEmpty() ? null : posList.stream().mapToInt(Integer::intValue).average().orElse(0.0);

            // Weight 계산: V2의 정교화된 priorityScore와 유사한 복합 가중치 사용
            double frequencyScore = a == null ? 0.0 : (double) a.count / (double) totalMentorCount; // 0~1
            double mentorCoverageScore = (double) mentorCount / (double) totalMentorCount; // 0~1
            double positionScore = avgPos != null ? 1.0 / (avgPos + 1) : 0.0; // 0~1, 위치가 앞일수록 높은 점수

            // 연결성 점수: outgoing + incoming transitions (정규화)
            int outgoing = transitions.getOrDefault(k, Collections.emptyMap()).values().stream().mapToInt(Integer::intValue).sum();
            int incoming = transitions.entrySet().stream()
                    .mapToInt(e -> e.getValue().getOrDefault(k, 0)).sum();
            int totalTransitions = outgoing + incoming;
            double connectivityScore = totalTransitions > 0 ? Math.min(1.0, (double) totalTransitions / (totalMentorCount * 2)) : 0.0; // 0~1

            double weight =
                0.4 * frequencyScore +
                0.3 * mentorCoverageScore +
                0.2 * positionScore +
                0.1 * connectivityScore;

            // 방어적 코딩: 0~1 범위로 클램프
            weight = Math.max(0.0, Math.min(1.0, weight));

            // 다음으로 이어지는 노드들의 분포 (JSON으로 저장)
            Map<String, Integer> outMap = transitions.getOrDefault(k, Collections.emptyMap());
            String transitionCountsJson = null;
            if (!outMap.isEmpty()) {
                transitionCountsJson = Ut.json.toString(outMap);
            }

            // 대안 부모 정보 저장 (JSON으로 저장)
            List<AlternativeParentInfo> altParents = skippedParents.get(k);
            String alternativeParentsJson = null;
            if (altParents != null && !altParents.isEmpty()) {
                alternativeParentsJson = Ut.json.toString(altParents);
            }

            JobRoadmapNodeStat stat = JobRoadmapNodeStat.builder()
                    .node(persisted)
                    .stepOrder(persisted.getStepOrder())
                    .weight(weight)
                    .averagePosition(avgPos)
                    .mentorCount(mentorCount)
                    .totalMentorCount(totalMentorCount)
                    .mentorCoverageRatio(mentorCoverageScore)
                    .outgoingTransitions(outgoing)
                    .incomingTransitions(incoming)
                    .transitionCounts(transitionCountsJson)
                    .alternativeParents(alternativeParentsJson)
                    .build();

            stats.add(stat);
        }
        jobRoadmapNodeStatRepository.saveAll(stats);
        
        log.info("JobRoadmap 생성 완료: id={}, 노드={}개, 통계={}개", saved.getId(), saved.getNodes().size(), stats.size());

        return saved;
    }

    // ---------------- 헬퍼 클래스&메서드 ----------------

    private static class AggregatedNode {
        Task task;
        String displayName;
        int count = 0;
        AggregatedNode(Task task, String displayName) { this.task = task; this.displayName = displayName; }
    }

    private static class AlternativeParentInfo {
        public String parentKey;           // 부모 노드 키 (T:1, N:kotlin 등)
        public int transitionCount;        // 전이 빈도
        public int mentorCount;            // 해당 부모를 사용한 멘토 수
        public double score;               // 가중치 점수

        public AlternativeParentInfo(String parentKey, int transitionCount, int mentorCount, double score) {
            this.parentKey = parentKey;
            this.transitionCount = transitionCount;
            this.mentorCount = mentorCount;
            this.score = score;
        }
    }

    private static class ParentCandidate {
        private final String parentKey;
        private final int transitionCount;
        private final double priorityScore;
        public ParentCandidate(String parentKey, int transitionCount, double priorityScore) {
            this.parentKey = parentKey;
            this.transitionCount = transitionCount;
            this.priorityScore = priorityScore;
        }
        public String getParentKey() { return parentKey; }
        public double getPriorityScore() { return priorityScore; }
    }

    private static class DeferredChild {
        String parentKey;
        String childKey;
        int retryCount;
        public DeferredChild(String parentKey, String childKey, int retryCount) {
            this.parentKey = parentKey;
            this.childKey = childKey;
            this.retryCount = retryCount;
        }
    }

    private void recordAsAlternative(String parentKey, String childKey,
                                     Map<String, Map<String, Integer>> transitions,
                                     Map<String, Set<Long>> mentorAppearSet,
                                     Map<String, List<Integer>> positions,
                                     int totalMentorCount,
                                     Map<String, List<AlternativeParentInfo>> skippedParents) {
        int transitionCount = transitions.getOrDefault(parentKey, Collections.emptyMap()).getOrDefault(childKey, 0);
        int parentMentorCount = mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size();

        // V2의 정교화된 priorityScore와 동일한 복합 점수 계산 (일관성 보장)
        double transitionPopularity = (double) transitionCount / totalMentorCount;

        List<Integer> parentPosList = positions.getOrDefault(parentKey, Collections.emptyList());
        double avgParentPos = parentPosList.isEmpty() ? 99.0
            : parentPosList.stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double positionScore = 1.0 / (avgParentPos + 1);

        double mentorCoverageScore = (double) parentMentorCount / totalMentorCount;

        // V2 가중치 사용 (V1과 다름)
        double score =
            W_TRANSITION_POPULARITY * transitionPopularity +
            W_POSITION_SIMILARITY * positionScore +
            W_MENTOR_COVERAGE * mentorCoverageScore;

        AlternativeParentInfo info = new AlternativeParentInfo(parentKey, transitionCount, parentMentorCount, score);
        skippedParents.computeIfAbsent(childKey, k -> new ArrayList<>()).add(info);
    }

    private String mergeTopDescriptions(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : list) {
            if (s == null) continue;
            String c = s.trim();
            if (!c.isEmpty()) set.add(c);
            if (set.size() >= 3) break;
        }
        return String.join("\n\n", set);
    }

    private Double calculateAverage(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private Integer calculateIntegerAverage(List<Integer> list) {
        Double avg = calculateAverage(list);
        return avg != null ? (int) Math.round(avg) : null;
    }

    private String generateKey(RoadmapNode rn) {
        if (rn.getTask() != null) {
            return "T:" + rn.getTask().getId();
        }
        String name = rn.getTaskName();
        if (name == null || name.trim().isEmpty()) return "N:__unknown__";
        return "N:" + name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * 멘토 로드맵의 Task 표준화율 계산
     * @param roadmap 멘토 로드맵
     * @return 표준화율 (0.0 ~ 1.0)
     */
    private double calculateStandardizationRate(MentorRoadmap roadmap) {
        List<RoadmapNode> nodes = roadmap.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }

        long standardizedCount = nodes.stream()
                .filter(n -> n.getTask() != null)
                .count();

        return (double) standardizedCount / nodes.size();
    }

    /**
     * 멘토 로드맵의 품질 점수 계산
     * 품질 점수 = (노드 개수 점수 × 0.3) + (표준화율 × 0.7)
     *
     * @param roadmap 멘토 로드맵
     * @return 품질 점수 (0.0 ~ 1.0)
     */
    private double calculateRoadmapQuality(MentorRoadmap roadmap) {
        List<RoadmapNode> nodes = roadmap.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }

        // 노드 개수 점수 (3개=0.0, 15개=1.0)
        int nodeCount = nodes.size();
        double nodeScore = Math.min(1.0, (nodeCount - 3.0) / 12.0);

        // 표준화율
        double standardizationScore = calculateStandardizationRate(roadmap);

        // 복합 점수 (노드 개수 30%, 표준화율 70%)
        return QUALITY_NODE_COUNT_WEIGHT * nodeScore + QUALITY_STANDARDIZATION_WEIGHT * standardizationScore;
    }
}
