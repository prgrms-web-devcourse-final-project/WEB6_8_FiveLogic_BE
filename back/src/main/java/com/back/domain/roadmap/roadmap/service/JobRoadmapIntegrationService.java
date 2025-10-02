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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapIntegrationService {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final JobRepository jobRepository;
    private final JobRoadmapRepository jobRoadmapRepository;
    private final TaskRepository taskRepository;
    private final JobRoadmapNodeStatRepository jobRoadmapNodeStatRepository;

    private final double BRANCH_THRESHOLD = 0.25;
    private final int MAX_DEPTH = 10;
    private final int MAX_CHILDREN = 4;

    // 부모 우선순위 점수 가중치
    private static final double TRANSITION_WEIGHT = 0.7;      // 전이 빈도 가중치
    private static final double POSITION_WEIGHT = 0.2;        // 위치 가중치
    private static final double MENTOR_COVERAGE_WEIGHT = 0.1; // 멘토 커버리지 가중치
    private static final int MAX_DEFERRED_RETRY = 3;          // 재시도 최대 횟수

    @Transactional
    public JobRoadmap integrateJobRoadmap(Long jobId) {
        // 해당 Job 존재 확인
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ServiceException("404", "직업을 찾을 수 없습니다. id=" + jobId));

        // 옵션: 기존 JobRoadmap 삭제 
        jobRoadmapRepository.findByJob(job).ifPresent(existing -> {
            jobRoadmapRepository.delete(existing);
            log.info("기존 JobRoadmap 삭제: id={}", existing.getId());
        });

        List<MentorRoadmap> mentorRoadmaps = mentorRoadmapRepository.findAllByMentorJobIdWithNodes(jobId);

        // 저품질 로드맵 필터링 (노드 3개 이하 제외)
        List<MentorRoadmap> qualityFiltered = mentorRoadmaps.stream()
                .filter(mr -> mr.getNodes() != null && mr.getNodes().size() >= 3)
                .toList();

        if (qualityFiltered.isEmpty()) {
            throw new ServiceException("404", "해당 직업에 대한 유효한 멘토 로드맵이 존재하지 않습니다. (최소 3개 노드 필요)");
        }

        log.info("멘토 로드맵 필터링: 전체 {}개 → 유효 {}개", mentorRoadmaps.size(), qualityFiltered.size());
        mentorRoadmaps = qualityFiltered;
        final int totalMentorCount = mentorRoadmaps.size(); // 통합에 사용될 멘토 로드맵 개수

        // 집계 자료구조
        Map<String, AggregatedNode> agg = new HashMap<>();
        Map<String, Map<String, Integer>> transitions = new HashMap<>();
        Map<String, Integer> rootCount = new HashMap<>();
        Map<String, Set<Long>> mentorAppearSet = new HashMap<>(); // unique mentors per key
        Map<String, List<Integer>> positions = new HashMap<>(); // to compute average position
        Map<String, List<String>> learningAdvices = new HashMap<>();
        Map<String, List<String>> recommendedResourcesList = new HashMap<>();
        Map<String, List<String>> learningGoalsList = new HashMap<>();
        Map<String, List<Integer>> difficulties = new HashMap<>();
        Map<String, List<Integer>> importances = new HashMap<>();
        Map<String, List<Integer>> estimatedHoursList = new HashMap<>();

        // 집계 루프(모든 멘토 로드맵의 모든 노드 순회하며 필요한 통계 자료 수집)
        for (MentorRoadmap mr : mentorRoadmaps) {
            // 멘토 로드맵의 노드를 순서대로 정렬 (stepOrder 기준)
            List<RoadmapNode> nodes = mr.getNodes().stream()
                    .sorted(Comparator.comparingInt(RoadmapNode::getStepOrder))
                    .toList();

            if (nodes.isEmpty()) continue;

            // 첫번째 노드를 root 후보로 등록
            RoadmapNode first = nodes.get(0);
            String firstKey = generateKey(first);
            rootCount.merge(firstKey, 1, Integer::sum);

            // 멘토 ID를 저장 (노드별 mentorAppearSet에 활용)
            Long mentorId = mr.getMentor().getId();

            // 각 노드 집계
            for (int i = 0; i < nodes.size(); i++) {
                RoadmapNode rn = nodes.get(i);
                String k = generateKey(rn);

                // agg: 노드별 출현 횟수(빈도)
                AggregatedNode an = agg.computeIfAbsent(k, kk -> new AggregatedNode(rn.getTask(), rn.getTask() != null ? rn.getTask().getName() : rn.getTaskName()));
                an.count += 1;

                // positions: 현재 위치 기록(1부터 시작)
                positions.computeIfAbsent(k, kk -> new ArrayList<>()).add(i + 1);

                // metorAppearSet: 고유 멘토 수
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

                // transitions: 다음 노드로의 이동 기록 (A->B가 몇 번 나왔는지)
                if (i < nodes.size() - 1) {
                    String nextKey = generateKey(nodes.get(i + 1));
                    transitions.computeIfAbsent(k, kk -> new HashMap<>()).merge(nextKey, 1, Integer::sum);
                }
            }
        }

        // Task prefetch (통합된 결과를 만들 때 필요한 task들 한번에 로드)
        Set<Long> taskIds = agg.values().stream()
                .map(a -> a.task != null ? a.task.getId() : null)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Task> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) taskRepository.findAllById(taskIds).forEach(t -> taskMap.put(t.getId(), t));

        // RootKey 선택: 가장 많이 루트로 등장한 키를 root로 설정 (동점 시 사전순 tie-breaker)
        String rootKey = rootCount.entrySet().stream()
                .sorted((e1, e2) -> {
                    int cmp = Integer.compare(e2.getValue(), e1.getValue());
                    if (cmp != 0) return cmp;
                    return e1.getKey().compareTo(e2.getKey()); // tie-breaker
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseGet(() -> agg.entrySet().stream()
                        .sorted((e1, e2) -> {
                            int cmp = Integer.compare(e2.getValue().count, e1.getValue().count);
                            if (cmp != 0) return cmp;
                            return e1.getKey().compareTo(e2.getKey());
                        })
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseThrow());

        log.info("선택된 rootKey={} (빈도={})", rootKey, rootCount.getOrDefault(rootKey, 0));

        // 노드 생성 (집계된 데이터를 기반으로 RoadmapNode 인스턴스 생성)
        Map<String, RoadmapNode> keyToNode = new HashMap<>();
        for (Map.Entry<String, AggregatedNode> e : agg.entrySet()) {
            AggregatedNode a = e.getValue();
            String key = e.getKey();

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
                    .task(a.task != null ? taskMap.get(a.task.getId()) : null)
                    .stepOrder(0) // assign later
                    .level(0)     // assign later via addChild/setLevel
                    .roadmapId(0L)
                    .roadmapType(RoadmapType.JOB)
                    .build();
            keyToNode.put(key, node);
        }

        // Transition 빈도 기반 자식 선택 + 부모 우선순위 계산
        // 주의: chosenChildren은 BRANCH_THRESHOLD로 필터링되어 일부 저빈도 전이는 제외됨
        // childToParentCandidates는 모든 전이를 포함 (부모 우선순위 분석용)
        // 결과적으로 chosenChildren에 포함되지 않은 노드는 메인 트리에서 제외되며,
        // topLevelNodes 처리에서 별도 루트로 추가됨 (의도된 설계: 저빈도 경로는 분리된 트리로 표현)
        Map<String, List<String>> chosenChildren = new HashMap<>();
        Map<String, List<ParentCandidate>> childToParentCandidates = new HashMap<>(); // 자식별 부모 후보 목록

        for (Map.Entry<String, Map<String, Integer>> e : transitions.entrySet()) {
            String parent = e.getKey();
            Map<String, Integer> cmap = e.getValue();
            int parentCount = agg.getOrDefault(parent, new AggregatedNode(null, parent)).count;
            List<Map.Entry<String, Integer>> sorted = cmap.entrySet().stream()
                    .sorted((x, y) -> y.getValue().compareTo(x.getValue()))
                    .limit(MAX_CHILDREN)
                    .toList();

            List<String> chosen = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                Map.Entry<String, Integer> ce = sorted.get(i);
                String child = ce.getKey();
                int transitionCount = ce.getValue();

                if (i == 0) {
                    chosen.add(child);
                } else {
                    double ratio = parentCount == 0 ? 0.0 : (double) transitionCount / parentCount;
                    // 현재: 부모 대비 비율만 체크 (샘플 데이터가 적을 때 유용)
                    if (ratio >= BRANCH_THRESHOLD) chosen.add(child);

                    // TODO: 데이터 충분히 확보 후 활성화 - 전체 멘토 대비 비율로 저빈도 노이즈 필터링
                    // double globalRatio = (double) transitionCount / totalMentorCount;
                    // if (ratio >= BRANCH_THRESHOLD && globalRatio >= 0.2) {  // 전체 멘토의 20% 이상
                    //     chosen.add(child);
                    // }
                }

                // 부모 후보 점수 계산 (정규화된 복합 점수)
                double transitionScore = (double) transitionCount / totalMentorCount; // 0~1

                List<Integer> parentPosList = positions.getOrDefault(parent, Collections.emptyList());
                double avgParentPos = parentPosList.isEmpty() ? 99.0 : parentPosList.stream().mapToInt(Integer::intValue).average().orElse(99.0);
                double positionScore = 1.0 / (avgParentPos + 1); // 0~1, 위치가 앞일수록 높은 점수

                int parentMentorCount = mentorAppearSet.getOrDefault(parent, Collections.emptySet()).size();
                double mentorCoverageScore = (double) parentMentorCount / totalMentorCount; // 0~1

                double priorityScore =
                    TRANSITION_WEIGHT * transitionScore +
                    POSITION_WEIGHT * positionScore +
                    MENTOR_COVERAGE_WEIGHT * mentorCoverageScore;

                childToParentCandidates.computeIfAbsent(child, k -> new ArrayList<>())
                        .add(new ParentCandidate(parent, transitionCount, priorityScore));
            }
            if (!chosen.isEmpty()) chosenChildren.put(parent, chosen);
        }

        // 각 자식에 대해 부모 후보를 점수순으로 정렬 (최고 점수 부모가 먼저 선택되도록)
        Map<String, String> childToBestParent = new HashMap<>();
        for (Map.Entry<String, List<ParentCandidate>> entry : childToParentCandidates.entrySet()) {
            String child = entry.getKey();
            List<ParentCandidate> candidates = entry.getValue();
            if (!candidates.isEmpty()) {
                // tie-breaker: 동일 점수일 때 parentKey 사전순으로 정렬 (결정성 보장)
                candidates.sort((a, b) -> {
                    int scoreDiff = Double.compare(b.priorityScore, a.priorityScore);
                    if (scoreDiff == 0) {
                        return a.parentKey.compareTo(b.parentKey); // tie-breaker
                    }
                    return scoreDiff;
                });
                childToBestParent.put(child, candidates.get(0).parentKey);
            }
        }

        log.debug("부모 후보 샘플 (상위 10개): {}",
            childToBestParent.entrySet().stream().limit(10).collect(Collectors.toList()));

        // BFS 탐색을 통해 parent-child 연결 생성 -> 메인 트리 구성
        if (!keyToNode.containsKey(rootKey)) {
            // fallback root 생성
            AggregatedNode a = agg.get(rootKey);
            RoadmapNode rootNode = RoadmapNode.builder()
                    .taskName(a != null ? a.displayName : "root")
                    .task(a != null ? a.task : null)
                    .stepOrder(0)
                    .level(0)
                    .roadmapId(0L)
                    .roadmapType(RoadmapType.JOB)
                    .build();
            keyToNode.put(rootKey, rootNode);
        }
        RoadmapNode rootNode = keyToNode.get(rootKey);

        // BFS queue + visited 집합 + 대안 부모 추적 + 재시도 큐
        Queue<String> q = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, List<AlternativeParentInfo>> skippedParents = new HashMap<>();
        Queue<DeferredChild> deferredQueue = new ArrayDeque<>();

        visited.add(rootKey);
        q.add(rootKey);

        // 1차 BFS: 형제 순서대로 stepOrder 지정 (부모 우선순위 고려)
        while (!q.isEmpty()) {
            String pk = q.poll();
            RoadmapNode parentNode = keyToNode.get(pk);
            List<String> childs = chosenChildren.getOrDefault(pk, Collections.emptyList());
            int order = 1;
            for (String ck : childs) {
                if (visited.contains(ck)) {
                    // 이미 다른 부모와 연결된 노드 -> 대안 부모로 기록
                    recordAsAlternative(pk, ck, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    continue;
                }

                RoadmapNode childNode = keyToNode.get(ck);
                if (childNode == null) continue;

                // 부모 우선순위 체크: 현재 부모가 최적의 부모인지 확인
                String bestParent = childToBestParent.get(ck);
                if (bestParent != null && !bestParent.equals(pk)) {
                    if (!visited.contains(bestParent)) {
                        // bestParent가 아직 미방문 -> 나중에 재시도
                        deferredQueue.add(new DeferredChild(pk, ck, MAX_DEFERRED_RETRY));
                        continue;
                    }
                    // bestParent가 이미 방문됨 -> 대안으로 기록
                    recordAsAlternative(pk, ck, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    continue;
                }

                // MAX_DEPTH 제한 체크
                if (parentNode.getLevel() + 1 > MAX_DEPTH) {
                    log.warn("MAX_DEPTH({}) 초과로 노드 추가 중단: parent={}, child={}", MAX_DEPTH, pk, ck);
                    recordAsAlternative(pk, ck, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    continue;
                }

                // 연결 수행
                visited.add(ck);
                parentNode.addChild(childNode);
                childNode.setStepOrder(order++);
                q.add(ck);
            }
        }

        // 2차: Deferred 재시도 (bestParent가 이제 방문 가능한지 확인)
        int deferredProcessed = 0;
        while (!deferredQueue.isEmpty()) {
            DeferredChild dc = deferredQueue.poll();
            if (visited.contains(dc.childKey)) continue; // 이미 연결됨

            String bestParent = childToBestParent.get(dc.childKey);
            if (bestParent != null && visited.contains(bestParent)) {
                // bestParent가 이제 방문됨 -> 연결 가능
                RoadmapNode bestParentNode = keyToNode.get(bestParent);
                RoadmapNode childNode = keyToNode.get(dc.childKey);

                if (bestParentNode != null && childNode != null && bestParentNode.getLevel() + 1 <= MAX_DEPTH) {
                    visited.add(dc.childKey);
                    bestParentNode.addChild(childNode);
                    int childCount = bestParentNode.getChildren().size();
                    childNode.setStepOrder(childCount);
                    deferredProcessed++;
                    log.debug("Deferred 연결 성공: {} -> {}", bestParent, dc.childKey);
                } else {
                    // Depth 초과 등 -> 대안 기록
                    recordAsAlternative(dc.parentKey, dc.childKey, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                }
            } else {
                // 여전히 미방문 -> 재시도 또는 fallback
                if (dc.retryCount > 0) {
                    deferredQueue.add(new DeferredChild(dc.parentKey, dc.childKey, dc.retryCount - 1));
                } else {
                    // 최종 fallback: 원래 부모와 연결 시도
                    RoadmapNode fallbackParentNode = keyToNode.get(dc.parentKey);
                    RoadmapNode childNode = keyToNode.get(dc.childKey);

                    if (fallbackParentNode != null && childNode != null &&
                        fallbackParentNode.getLevel() + 1 <= MAX_DEPTH && !visited.contains(dc.childKey)) {
                        visited.add(dc.childKey);
                        fallbackParentNode.addChild(childNode);
                        int childCount = fallbackParentNode.getChildren().size();
                        childNode.setStepOrder(childCount);
                        log.debug("Fallback 연결: {} -> {}", dc.parentKey, dc.childKey);
                    } else {
                        // 최종 실패 -> 대안 기록
                        recordAsAlternative(dc.parentKey, dc.childKey, transitions, mentorAppearSet, positions, totalMentorCount, skippedParents);
                    }
                }
            }
        }

        log.info("Deferred 재시도 완료: {}개 노드 연결", deferredProcessed);

        // 메인 루트 설정 (단일 루트만 허용)
        RoadmapNode mainRoot = keyToNode.get(rootKey);
        if (mainRoot != null) {
            mainRoot.setStepOrder(1);
            mainRoot.setLevel(0);
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

        // JobRoadmap 생성
        JobRoadmap jobRoadmap = JobRoadmap.builder().job(job).build();

        // visited된 노드만 포함 (고아 노드 제외)
        List<RoadmapNode> allNodes = keyToNode.values().stream()
                .filter(n -> visited.contains(generateKey(n)))  // 메인 트리에 연결된 노드만
                .sorted(Comparator.comparingInt(RoadmapNode::getLevel).thenComparingInt(RoadmapNode::getStepOrder))
                .toList();

        // jobRoadmap 저장해 ID 받아옴
        jobRoadmap = jobRoadmapRepository.save(jobRoadmap);
        Long roadmapId = jobRoadmap.getId();

        // 모든 노드에 roadmapId, roadmapType 설정 후 JobRoadmap의 노드로 추가
        for (RoadmapNode n : allNodes) {
            n.setRoadmapId(roadmapId);
            n.setRoadmapType(RoadmapType.JOB);
            jobRoadmap.getNodes().add(n);
        }

        JobRoadmap saved = jobRoadmapRepository.save(jobRoadmap);

        // 노드별 통계값 저장
        List<JobRoadmapNodeStat> stats = new ArrayList<>();
        for (RoadmapNode persisted : saved.getNodes()) {
            String k = generateKey(persisted);
            AggregatedNode a = agg.get(k);
            JobRoadmapNodeStat stat = new JobRoadmapNodeStat();
            stat.setNode(persisted);
            stat.setStepOrder(persisted.getStepOrder());

            // 해당 키를 선택한 멘토 수
            int mentorCount = mentorAppearSet.getOrDefault(k, Collections.emptySet()).size();
            stat.setMentorCount(mentorCount);

            // 평균 단계 위치
            List<Integer> posList = positions.getOrDefault(k, Collections.emptyList());
            Double avgPos = posList.isEmpty() ? null : posList.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            stat.setAveragePosition(avgPos);

            // Weight 계산: priorityScore와 동일한 복합 가중치 사용
            // (등장빈도, 멘토커버리지, 평균위치, 연결성)
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

            stat.setWeight(weight);
            stat.setTotalMentorCount(totalMentorCount);
            stat.setMentorCoverageRatio(mentorCoverageScore); // 0.0 ~ 1.0
            stat.setOutgoingTransitions(outgoing);
            stat.setIncomingTransitions(incoming);

            // 다음으로 이어지는 노드들의 분포 (Ut.json 사용)
            Map<String, Integer> outMap = transitions.getOrDefault(k, Collections.emptyMap());
            if (!outMap.isEmpty()) {
                String json = Ut.json.toString(outMap);
                if (json != null) {
                    stat.setTransitionCounts(json);
                }
            }

            // 대안 부모 정보 저장 (메타정보 포함, Ut.json 사용)
            List<AlternativeParentInfo> altParents = skippedParents.get(k);
            if (altParents != null && !altParents.isEmpty()) {
                String json = Ut.json.toString(altParents);
                if (json != null) {
                    stat.setAlternativeParents(json);
                }
            }

            stats.add(stat);
        }

        jobRoadmapNodeStatRepository.saveAll(stats);
        log.info("JobRoadmap 생성 완료: id={}, 노드={}개, 통계={}개, 대안 부모 기록={}개",
            saved.getId(), saved.getNodes().size(), stats.size(), skippedParents.size());

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
        String parentKey;
        int transitionCount;
        double priorityScore;

        public ParentCandidate(String parentKey, int transitionCount, double priorityScore) {
            this.parentKey = parentKey;
            this.transitionCount = transitionCount;
            this.priorityScore = priorityScore;
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

    private void recordAsAlternative(String parentKey, String childKey,
                                     Map<String, Map<String, Integer>> transitions,
                                     Map<String, Set<Long>> mentorAppearSet,
                                     Map<String, List<Integer>> positions,
                                     int totalMentorCount,
                                     Map<String, List<AlternativeParentInfo>> skippedParents) {
        int transitionCount = transitions.getOrDefault(parentKey, Collections.emptyMap()).getOrDefault(childKey, 0);
        int parentMentorCount = mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size();

        // priorityScore와 동일한 복합 점수 계산 (일관성 보장)
        double transitionScore = (double) transitionCount / totalMentorCount;

        List<Integer> parentPosList = positions.getOrDefault(parentKey, Collections.emptyList());
        double avgParentPos = parentPosList.isEmpty() ? 99.0
            : parentPosList.stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double positionScore = 1.0 / (avgParentPos + 1);

        double mentorCoverageScore = (double) parentMentorCount / totalMentorCount;

        double score =
            TRANSITION_WEIGHT * transitionScore +
            POSITION_WEIGHT * positionScore +
            MENTOR_COVERAGE_WEIGHT * mentorCoverageScore;

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

    // Integer 리스트의 평균을 Double로 반환
    private Double calculateAverage(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    // Integer 리스트의 평균을 반올림하여 Integer로 반환
    private Integer calculateIntegerAverage(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        double avg = list.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        return (int) Math.round(avg);
    }

    // RoadmapNode를 고유한 키로 변환
    private String generateKey(RoadmapNode rn) {
        // Task가 연결된 경우: "T:{taskId}"
        if (rn.getTask() != null) {
            return "T:" + rn.getTask().getId();
        }

        //Task 없는 경우: "N:{taskName}" (소문자, 공백 정규화)
        String name = rn.getTaskName();

        // 빈 taskName 방어: "N:__unknown__"
        if (name == null) return "N:__unknown__";
        name = name.trim().toLowerCase();
        if (name.isEmpty()) return "N:__unknown__";
        return "N:" + name.replaceAll("\\s+", " ");
    }
}
