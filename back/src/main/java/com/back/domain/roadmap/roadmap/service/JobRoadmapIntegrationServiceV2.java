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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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


    // ========================================
    // Public API
    // ========================================

    @Transactional
    public JobRoadmap integrateJobRoadmap(Long jobId) {
        // 1. 데이터 준비
        Job job = validateAndGetJob(jobId);
        deleteExistingJobRoadmap(job);
        List<MentorRoadmap> mentorRoadmaps = loadAndFilterMentorRoadmaps(jobId);

        // 2. 통계 집계
        AggregationResult aggregation = aggregateStatistics(mentorRoadmaps);

        // 3. 트리 구성
        TreeBuildResult treeResult = buildMainTree(aggregation);

        // 4. 로그 및 영속화
        logOrphanNodes(treeResult, aggregation);
        return persistJobRoadmap(job, treeResult, aggregation);
    }


    // ========================================
    // Step 1: 데이터 준비 및 필터링
    // ========================================

    private Job validateAndGetJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ServiceException("404", "직업을 찾을 수 없습니다. id=" + jobId));
    }

    private void deleteExistingJobRoadmap(Job job) {
        jobRoadmapRepository.findByJob(job).ifPresent(existing -> {
            jobRoadmapRepository.delete(existing);
            log.info("기존 JobRoadmap 삭제: id={}", existing.getId());
        });
    }

    private List<MentorRoadmap> loadAndFilterMentorRoadmaps(Long jobId) {
        List<MentorRoadmap> all = mentorRoadmapRepository.findAllByMentorJobIdWithNodes(jobId);

        List<MentorRoadmap> filtered = all.stream()
                .filter(mr -> mr.getNodes() != null && mr.getNodes().size() >= 3)
                .filter(mr -> calculateStandardizationRate(mr) >= MIN_STANDARDIZATION_RATE)
                .filter(mr -> calculateRoadmapQuality(mr) >= MIN_QUALITY_THRESHOLD)
                .toList();

        if (filtered.isEmpty()) {
            throw new ServiceException("404", "해당 직업에 대한 유효한 멘토 로드맵이 존재하지 않습니다. " +
                    "(최소 조건: 노드 3개 이상, 표준화율 " + (int)(MIN_STANDARDIZATION_RATE * 100) + "% 이상, 품질 점수 " + MIN_QUALITY_THRESHOLD + " 이상)");
        }

        log.info("멘토 로드맵 품질 필터링: 전체 {}개 → 유효 {}개 (노드 3개 이상, 표준화율 {}% 이상, 품질 점수 {} 이상)",
                all.size(), filtered.size(), (int)(MIN_STANDARDIZATION_RATE * 100), MIN_QUALITY_THRESHOLD);

        return filtered;
    }


    // ========================================
    // Step 2: 통계 집계
    // ========================================

    private AggregationResult aggregateStatistics(List<MentorRoadmap> mentorRoadmaps) {
        AggregationResult result = new AggregationResult(mentorRoadmaps.size());

        for (MentorRoadmap mr : mentorRoadmaps) {
            List<RoadmapNode> nodes = getSortedNodes(mr);
            if (nodes.isEmpty()) continue;

            aggregateRootCandidate(nodes.get(0), result);
            aggregateNodesFromRoadmap(mr, nodes, result);
        }

        return result;
    }

    private List<RoadmapNode> getSortedNodes(MentorRoadmap mr) {
        return mr.getNodes().stream()
                .sorted(Comparator.comparingInt(RoadmapNode::getStepOrder))
                .toList();
    }

    private void aggregateRootCandidate(RoadmapNode first, AggregationResult result) {
        result.rootCount.merge(generateKey(first), 1, Integer::sum);
    }

    private void aggregateNodesFromRoadmap(MentorRoadmap mr, List<RoadmapNode> nodes, AggregationResult result) {
        Long mentorId = mr.getMentor().getId();

        for (int i = 0; i < nodes.size(); i++) {
            RoadmapNode rn = nodes.get(i);
            String key = generateKey(rn);

            aggregateNodeStatistics(rn, key, i + 1, mentorId, result);
            aggregateDescriptions(rn, key, result.descriptions);

            if (i < nodes.size() - 1) {
                aggregateTransition(key, generateKey(nodes.get(i + 1)), result);
            }
        }
    }

    private void aggregateNodeStatistics(RoadmapNode rn, String key, int position, Long mentorId, AggregationResult result) {
        result.agg.computeIfAbsent(key, kk -> new AggregatedNode(
                rn.getTask(),
                rn.getTask() != null ? rn.getTask().getName() : rn.getTaskName()
        )).count++;

        result.positions.computeIfAbsent(key, kk -> new ArrayList<>()).add(position);
        result.mentorAppearSet.computeIfAbsent(key, kk -> new HashSet<>()).add(mentorId);
    }

    private void aggregateDescriptions(RoadmapNode rn, String key, DescriptionCollections descriptions) {
        if (rn.getLearningAdvice() != null && !rn.getLearningAdvice().isBlank()) {
            descriptions.learningAdvices.computeIfAbsent(key, kk -> new ArrayList<>()).add(rn.getLearningAdvice());
        }

        if (rn.getRecommendedResources() != null && !rn.getRecommendedResources().isBlank()) {
            descriptions.recommendedResources.computeIfAbsent(key, kk -> new ArrayList<>()).add(rn.getRecommendedResources());
        }

        if (rn.getLearningGoals() != null && !rn.getLearningGoals().isBlank()) {
            descriptions.learningGoals.computeIfAbsent(key, kk -> new ArrayList<>()).add(rn.getLearningGoals());
        }

        if (rn.getDifficulty() != null) {
            descriptions.difficulties.computeIfAbsent(key, kk -> new ArrayList<>()).add(rn.getDifficulty());
        }

        if (rn.getImportance() != null) {
            descriptions.importances.computeIfAbsent(key, kk -> new ArrayList<>()).add(rn.getImportance());
        }

        if (rn.getEstimatedHours() != null) {
            descriptions.estimatedHours.computeIfAbsent(key, kk -> new ArrayList<>()).add(rn.getEstimatedHours());
        }
    }

    private void aggregateTransition(String fromKey, String toKey, AggregationResult result) {
        result.transitions.computeIfAbsent(fromKey, kk -> new HashMap<>()).merge(toKey, 1, Integer::sum);
    }


    // ========================================
    // Step 3: 트리 구성
    // ========================================

    private TreeBuildResult buildMainTree(AggregationResult aggregation) {
        String rootKey = selectRootKey(aggregation);
        Map<Long, Task> taskMap = prefetchTasks(aggregation);
        Map<String, RoadmapNode> keyToNode = createNodes(aggregation, taskMap);
        ParentEvaluation parentEval = evaluateParentCandidates(aggregation, keyToNode);

        return constructTreeViaBFS(rootKey, keyToNode, parentEval, aggregation);
    }

    private String selectRootKey(AggregationResult aggregation) {
        String rootKey = aggregation.rootCount.entrySet().stream()
                .max(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElseGet(() -> aggregation.agg.entrySet().stream()
                        .max(Comparator.comparingInt(e -> e.getValue().count))
                        .map(Map.Entry::getKey)
                        .orElseThrow());

        log.info("선택된 rootKey={} (빈도={})", rootKey, aggregation.rootCount.getOrDefault(rootKey, 0));
        return rootKey;
    }

    /**
     * Task 엔티티를 일괄 로드하여 N+1 문제 방지
     * @param aggregation 집계 결과
     * @return taskId → Task 매핑
     */
    private Map<Long, Task> prefetchTasks(AggregationResult aggregation) {
        Set<Long> taskIds = aggregation.agg.values().stream()
                .map(a -> a.task != null ? a.task.getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Task> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            taskRepository.findAllById(taskIds).forEach(t -> taskMap.put(t.getId(), t));
        }

        return taskMap;
    }

    private Map<String, RoadmapNode> createNodes(AggregationResult aggregation, Map<Long, Task> taskMap) {
        Map<String, RoadmapNode> keyToNode = new HashMap<>();

        aggregation.agg.forEach((key, aggNode) -> {
            Double avgDifficulty = calculateAverage(aggregation.descriptions.difficulties.get(key));
            Double avgImportance = calculateAverage(aggregation.descriptions.importances.get(key));
            Integer avgEstimatedHours = calculateIntegerAverage(aggregation.descriptions.estimatedHours.get(key));

            RoadmapNode node = RoadmapNode.builder()
                    .taskName(aggNode.displayName)
                    .learningAdvice(mergeTopDescriptions(aggregation.descriptions.learningAdvices.get(key)))
                    .recommendedResources(mergeTopDescriptions(aggregation.descriptions.recommendedResources.get(key)))
                    .learningGoals(mergeTopDescriptions(aggregation.descriptions.learningGoals.get(key)))
                    .difficulty(avgDifficulty != null ? avgDifficulty.intValue() : null)
                    .importance(avgImportance != null ? avgImportance.intValue() : null)
                    .estimatedHours(avgEstimatedHours)
                    .task(aggNode.task != null ? taskMap.get(aggNode.task.getId()) : null)
                    .roadmapId(0L)
                    .roadmapType(RoadmapType.JOB)
                    .build();

            keyToNode.put(key, node);
        });

        return keyToNode;
    }

    private ParentEvaluation evaluateParentCandidates(AggregationResult aggregation, Map<String, RoadmapNode> keyToNode) {
        Map<String, List<String>> chosenChildren = new HashMap<>();
        Map<String, List<ParentCandidate>> childToParentCandidates = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> e : aggregation.transitions.entrySet()) {
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

                if (i == 0) {
                    chosen.add(childKey);
                } else {
                    double ratio = (double) transitionCount / aggregation.agg.get(parentKey).count;
                    if (ratio >= BRANCH_THRESHOLD) {
                        chosen.add(childKey);
                    }
                }

                double priorityScore = calculatePriorityScore(parentKey, childKey, transitionCount,
                        parentTotalTransitions, aggregation);

                childToParentCandidates.computeIfAbsent(childKey, k -> new ArrayList<>())
                        .add(new ParentCandidate(parentKey, transitionCount, priorityScore));
            }

            if (!chosen.isEmpty()) {
                chosenChildren.put(parentKey, chosen);
            }
        }

        Map<String, String> childToBestParent = new HashMap<>();
        childToParentCandidates.forEach((child, candidates) -> {
            candidates.sort(Comparator.comparingDouble(ParentCandidate::getPriorityScore).reversed()
                    .thenComparing(ParentCandidate::getParentKey));
            childToBestParent.put(child, candidates.get(0).parentKey);
        });

        return new ParentEvaluation(chosenChildren, childToBestParent);
    }

    private double calculatePriorityScore(String parentKey, String childKey, int transitionCount,
                                          int parentTotalTransitions, AggregationResult aggregation) {
        double transitionPopularity = (double) transitionCount / aggregation.totalMentorCount;
        double transitionStrength = (double) transitionCount / parentTotalTransitions;

        double avgParentPos = aggregation.positions.getOrDefault(parentKey, Collections.emptyList())
                .stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double avgChildPos = aggregation.positions.getOrDefault(childKey, Collections.emptyList())
                .stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double positionSimilarity = 1.0 / (1.0 + Math.abs(avgParentPos - avgChildPos));

        double mentorCoverage = (double) aggregation.mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size()
                / aggregation.totalMentorCount;

        return W_TRANSITION_POPULARITY * transitionPopularity +
                W_TRANSITION_STRENGTH * transitionStrength +
                W_POSITION_SIMILARITY * positionSimilarity +
                W_MENTOR_COVERAGE * mentorCoverage;
    }

    private TreeBuildResult constructTreeViaBFS(String rootKey, Map<String, RoadmapNode> keyToNode,
                                                 ParentEvaluation parentEval, AggregationResult aggregation) {
        TreeBuildResult result = new TreeBuildResult(rootKey, keyToNode);
        Queue<DeferredChild> deferredQueue = new ArrayDeque<>();

        performFirstPassBFS(result, parentEval, aggregation, deferredQueue);
        performDeferredRetry(result, parentEval, deferredQueue);
        initializeMainRoot(result);

        return result;
    }

    private void performFirstPassBFS(TreeBuildResult result, ParentEvaluation parentEval,
                                      AggregationResult aggregation, Queue<DeferredChild> deferredQueue) {
        Queue<String> q = new ArrayDeque<>();
        q.add(result.rootKey);

        while (!q.isEmpty()) {
            String pk = q.poll();
            RoadmapNode parentNode = result.keyToNode.get(pk);
            List<String> childs = parentEval.chosenChildren.getOrDefault(pk, Collections.emptyList());
            int order = 1;

            for (String ck : childs) {
                if (result.visited.contains(ck)) {
                    recordAsAlternative(pk, ck, aggregation, result);
                    continue;
                }

                RoadmapNode childNode = result.keyToNode.get(ck);
                if (childNode == null) continue;

                String bestParent = parentEval.childToBestParent.get(ck);
                if (bestParent != null && !bestParent.equals(pk)) {
                    if (!result.visited.contains(bestParent)) {
                        deferredQueue.add(new DeferredChild(pk, ck, MAX_DEFERRED_RETRY));
                    } else {
                        recordAsAlternative(pk, ck, aggregation, result);
                    }
                    continue;
                }

                if (parentNode.getLevel() + 1 >= MAX_DEPTH) {
                    log.warn("MAX_DEPTH({}) 초과로 노드 추가 중단: parent={}, child={}", MAX_DEPTH, pk, ck);
                    recordAsAlternative(pk, ck, aggregation, result);
                    continue;
                }

                result.visited.add(ck);
                parentNode.addChild(childNode);
                childNode.assignOrderInSiblings(order++);
                q.add(ck);
            }
        }
    }

    private void performDeferredRetry(TreeBuildResult result, ParentEvaluation parentEval,
                                       Queue<DeferredChild> deferredQueue) {
        int deferredProcessed = 0;

        while (!deferredQueue.isEmpty()) {
            DeferredChild dc = deferredQueue.poll();
            if (result.visited.contains(dc.childKey)) continue;

            String bestParent = parentEval.childToBestParent.get(dc.childKey);
            if (bestParent != null && result.visited.contains(bestParent)) {
                RoadmapNode bestParentNode = result.keyToNode.get(bestParent);
                RoadmapNode childNode = result.keyToNode.get(dc.childKey);

                if (bestParentNode != null && childNode != null && bestParentNode.getLevel() + 1 < MAX_DEPTH) {
                    result.visited.add(dc.childKey);
                    bestParentNode.addChild(childNode);
                    childNode.assignOrderInSiblings(bestParentNode.getChildren().size());
                    deferredProcessed++;
                }
            } else if (dc.retryCount > 0) {
                deferredQueue.add(new DeferredChild(dc.parentKey, dc.childKey, dc.retryCount - 1));
            }
        }

        log.info("Deferred 재시도 완료: {}개 노드 연결", deferredProcessed);
    }

    private void initializeMainRoot(TreeBuildResult result) {
        RoadmapNode mainRoot = result.keyToNode.get(result.rootKey);
        if (mainRoot != null) {
            mainRoot.initializeAsRoot();
        }
    }

    private void recordAsAlternative(String parentKey, String childKey, AggregationResult aggregation, TreeBuildResult result) {
        int transitionCount = aggregation.transitions.getOrDefault(parentKey, Collections.emptyMap())
                .getOrDefault(childKey, 0);
        int parentMentorCount = aggregation.mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size();

        double transitionPopularity = (double) transitionCount / aggregation.totalMentorCount;

        List<Integer> parentPosList = aggregation.positions.getOrDefault(parentKey, Collections.emptyList());
        double avgParentPos = parentPosList.isEmpty() ? 99.0
                : parentPosList.stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double positionScore = 1.0 / (avgParentPos + 1);

        double mentorCoverageScore = (double) parentMentorCount / aggregation.totalMentorCount;

        double score =
                W_TRANSITION_POPULARITY * transitionPopularity +
                        W_POSITION_SIMILARITY * positionScore +
                        W_MENTOR_COVERAGE * mentorCoverageScore;

        AlternativeParentInfo info = new AlternativeParentInfo(parentKey, transitionCount, parentMentorCount, score);
        result.skippedParents.computeIfAbsent(childKey, k -> new ArrayList<>()).add(info);
    }


    // ========================================
    // Step 4: 로그 및 영속화
    // ========================================

    private void logOrphanNodes(TreeBuildResult result, AggregationResult aggregation) {
        long orphanCount = result.keyToNode.values().stream()
                .filter(n -> !result.visited.contains(generateKey(n)))
                .count();

        if (orphanCount > 0) {
            log.info("=== 제외된 고아 노드 ===");
            result.keyToNode.entrySet().stream()
                    .filter(e -> !result.visited.contains(e.getKey()))
                    .forEach(e -> {
                        String key = e.getKey();
                        AggregatedNode aggNode = aggregation.agg.get(key);
                        int count = aggNode != null ? aggNode.count : 0;
                        int mentorCount = aggregation.mentorAppearSet.getOrDefault(key, Collections.emptySet()).size();
                        log.info("  - 키: {}, 이름: {}, 출현빈도: {}회, 멘토수: {}명",
                                key, e.getValue().getTaskName(), count, mentorCount);
                    });
            log.info("총 {}개의 저빈도 노드가 메인 트리에서 제외되었습니다.", orphanCount);
        }
    }

    private JobRoadmap persistJobRoadmap(Job job, TreeBuildResult treeResult, AggregationResult aggregation) {
        JobRoadmap jobRoadmap = jobRoadmapRepository.save(JobRoadmap.builder().job(job).build());
        Long roadmapId = jobRoadmap.getId();

        attachNodesToRoadmap(jobRoadmap, treeResult, roadmapId);
        JobRoadmap saved = jobRoadmapRepository.save(jobRoadmap);

        saveNodeStatistics(saved, treeResult, aggregation);

        log.info("JobRoadmap 생성 완료: id={}, 노드={}개", saved.getId(), saved.getNodes().size());
        return saved;
    }

    private void attachNodesToRoadmap(JobRoadmap roadmap, TreeBuildResult treeResult, Long roadmapId) {
        List<RoadmapNode> allNodes = treeResult.keyToNode.values().stream()
                .filter(n -> treeResult.visited.contains(generateKey(n)))
                .peek(n -> n.assignToRoadmap(roadmapId, RoadmapType.JOB))
                .toList();

        roadmap.getNodes().addAll(allNodes);
    }

    private void saveNodeStatistics(JobRoadmap roadmap, TreeBuildResult treeResult, AggregationResult aggregation) {
        List<JobRoadmapNodeStat> stats = roadmap.getNodes().stream()
                .map(node -> createNodeStat(node, treeResult, aggregation))
                .toList();

        jobRoadmapNodeStatRepository.saveAll(stats);
    }

    private JobRoadmapNodeStat createNodeStat(RoadmapNode node, TreeBuildResult treeResult, AggregationResult aggregation) {
        String key = generateKey(node);
        AggregatedNode aggNode = aggregation.agg.get(key);

        int mentorCount = aggregation.mentorAppearSet.getOrDefault(key, Collections.emptySet()).size();

        List<Integer> posList = aggregation.positions.getOrDefault(key, Collections.emptyList());
        Double avgPos = posList.isEmpty() ? null : posList.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        double frequencyScore = aggNode == null ? 0.0 : (double) aggNode.count / (double) aggregation.totalMentorCount;
        double mentorCoverageScore = (double) mentorCount / (double) aggregation.totalMentorCount;
        double positionScore = avgPos != null ? 1.0 / (avgPos + 1) : 0.0;

        int outgoing = aggregation.transitions.getOrDefault(key, Collections.emptyMap()).values().stream()
                .mapToInt(Integer::intValue).sum();
        int incoming = aggregation.transitions.entrySet().stream()
                .mapToInt(e -> e.getValue().getOrDefault(key, 0)).sum();
        int totalTransitions = outgoing + incoming;
        double connectivityScore = totalTransitions > 0 ? Math.min(1.0, (double) totalTransitions / (aggregation.totalMentorCount * 2)) : 0.0;

        double weight = 0.4 * frequencyScore +
                0.3 * mentorCoverageScore +
                0.2 * positionScore +
                0.1 * connectivityScore;

        weight = Math.max(0.0, Math.min(1.0, weight));

        Map<String, Integer> outMap = aggregation.transitions.getOrDefault(key, Collections.emptyMap());
        String transitionCountsJson = null;
        if (!outMap.isEmpty()) {
            transitionCountsJson = Ut.json.toString(outMap);
        }

        List<AlternativeParentInfo> altParents = treeResult.skippedParents.get(key);
        String alternativeParentsJson = null;
        if (altParents != null && !altParents.isEmpty()) {
            alternativeParentsJson = Ut.json.toString(altParents);
        }

        return JobRoadmapNodeStat.builder()
                .node(node)
                .stepOrder(node.getStepOrder())
                .weight(weight)
                .averagePosition(avgPos)
                .mentorCount(mentorCount)
                .totalMentorCount(aggregation.totalMentorCount)
                .mentorCoverageRatio(mentorCoverageScore)
                .outgoingTransitions(outgoing)
                .incomingTransitions(incoming)
                .transitionCounts(transitionCountsJson)
                .alternativeParents(alternativeParentsJson)
                .build();
    }


    // ========================================
    // 헬퍼 메서드
    // ========================================

    private String generateKey(RoadmapNode rn) {
        if (rn.getTask() != null) {
            return "T:" + rn.getTask().getId();
        }
        String name = rn.getTaskName();
        if (name == null || name.trim().isEmpty()) return "N:__unknown__";
        return "N:" + name.trim().toLowerCase().replaceAll("\\s+", " ");
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


    // ========================================
    // 중간 객체 (Internal DTOs)
    // ========================================

    /**
     * 통계 집계 결과를 담는 객체
     */
    @Getter
    private static class AggregationResult {
        final Map<String, AggregatedNode> agg = new HashMap<>();
        final Map<String, Map<String, Integer>> transitions = new HashMap<>();
        final Map<String, Integer> rootCount = new HashMap<>();
        final Map<String, Set<Long>> mentorAppearSet = new HashMap<>();
        final Map<String, List<Integer>> positions = new HashMap<>();
        final DescriptionCollections descriptions = new DescriptionCollections();
        final int totalMentorCount;

        AggregationResult(int totalMentorCount) {
            this.totalMentorCount = totalMentorCount;
        }
    }

    /**
     * Description 관련 필드들을 묶은 객체
     */
    @Getter
    private static class DescriptionCollections {
        final Map<String, List<String>> learningAdvices = new HashMap<>();
        final Map<String, List<String>> recommendedResources = new HashMap<>();
        final Map<String, List<String>> learningGoals = new HashMap<>();
        final Map<String, List<Integer>> difficulties = new HashMap<>();
        final Map<String, List<Integer>> importances = new HashMap<>();
        final Map<String, List<Integer>> estimatedHours = new HashMap<>();
    }

    /**
     * 트리 구성 결과를 담는 객체
     */
    @Getter
    private static class TreeBuildResult {
        final String rootKey;
        final Map<String, RoadmapNode> keyToNode;
        final Set<String> visited = new HashSet<>();
        final Map<String, List<AlternativeParentInfo>> skippedParents = new HashMap<>();

        TreeBuildResult(String rootKey, Map<String, RoadmapNode> keyToNode) {
            this.rootKey = rootKey;
            this.keyToNode = keyToNode;
            visited.add(rootKey);
        }
    }

    /**
     * 부모 후보 평가 결과를 담는 객체
     */
    @Getter
    private static class ParentEvaluation {
        final Map<String, List<String>> chosenChildren;
        final Map<String, String> childToBestParent;

        ParentEvaluation(Map<String, List<String>> chosenChildren,
                         Map<String, String> childToBestParent) {
            this.chosenChildren = chosenChildren;
            this.childToBestParent = childToBestParent;
        }
    }


    // ========================================
    // 헬퍼 클래스 (Helper Classes)
    // ========================================

    private static class AggregatedNode {
        Task task;
        String displayName;
        int count = 0;

        AggregatedNode(Task task, String displayName) {
            this.task = task;
            this.displayName = displayName;
        }
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

        public String getParentKey() {
            return parentKey;
        }

        public double getPriorityScore() {
            return priorityScore;
        }
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
}
