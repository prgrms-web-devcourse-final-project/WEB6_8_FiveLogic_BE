package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.response.TextFieldIntegrationResponse;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.task.entity.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 집계된 통계(AggregationResult)를 기반으로 로드맵 트리 구조를 생성합니다.
 * 핵심 통합 알고리즘을 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoadmapTreeBuilder {

    private final TextFieldIntegrationService textFieldIntegrationService;

    // --- 통합 알고리즘 상수 ---
    private final double BRANCH_THRESHOLD = 0.25;
    private final int MAX_DEPTH = 10;
    private final int MAX_CHILDREN = 4;
    private final int MAX_DEFERRED_RETRY = 3;

    // --- 부모 우선순위 점수(priorityScore) 가중치 ---
    private static final double W_TRANSITION_POPULARITY = 0.4;
    private static final double W_TRANSITION_STRENGTH = 0.3;
    private static final double W_POSITION_SIMILARITY = 0.2;
    private static final double W_MENTOR_COVERAGE = 0.1;

    public TreeBuildResult build(RoadmapAggregator.AggregationResult aggregation, Map<Long, Task> taskMap) {
        String rootKey = selectRootKey(aggregation);
        Map<String, RoadmapNode> keyToNode = createNodes(aggregation, taskMap);
        ParentEvaluation parentEval = evaluateParentCandidates(aggregation);

        return constructTreeViaBFS(rootKey, keyToNode, parentEval, aggregation);
    }

    private String selectRootKey(RoadmapAggregator.AggregationResult aggregation) {
        String rootKey = aggregation.rootCount.entrySet().stream()
                .max(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElseGet(() -> aggregation.agg.entrySet().stream()
                        .max(Comparator.comparingInt(e -> e.getValue().count))
                        .map(Map.Entry::getKey)
                        .orElseThrow(() -> new IllegalStateException("No nodes to select a root from.")));

        log.info("선택된 rootKey={} (빈도={})", rootKey, aggregation.rootCount.getOrDefault(rootKey, 0));
        return rootKey;
    }

    private Map<String, RoadmapNode> createNodes(RoadmapAggregator.AggregationResult aggregation, Map<Long, Task> taskMap) {
        // 1단계: 텍스트 필드 배치 통합
        Map<String, TextFieldIntegrationResponse> integratedTextsMap =
                integrateTextFieldsBatch(aggregation);

        // 2단계: 통합된 텍스트로 노드 생성
        return buildNodesFromIntegratedTexts(aggregation, taskMap, integratedTextsMap);
    }

    /**
     * 모든 노드의 텍스트 필드를 배치로 통합합니다.
     *
     * @param aggregation 집계 결과
     * @return key: 노드 키, value: 통합된 텍스트 필드
     */
    private Map<String, TextFieldIntegrationResponse> integrateTextFieldsBatch(
            RoadmapAggregator.AggregationResult aggregation
    ) {
        // 텍스트 데이터가 있는 노드만 수집
        Map<String, TextFieldIntegrationService.NodeTextData> nodeTextsMap = new HashMap<>();
        Map<String, TextFieldIntegrationResponse> emptyResponseMap = new HashMap<>();

        aggregation.agg.forEach((key, aggNode) -> {
            List<String> advices = aggregation.descriptions.getLearningAdvices()
                    .getOrDefault(key, Collections.emptyList());
            List<String> resources = aggregation.descriptions.getRecommendedResources()
                    .getOrDefault(key, Collections.emptyList());
            List<String> goals = aggregation.descriptions.getLearningGoals()
                    .getOrDefault(key, Collections.emptyList());

            // 텍스트 데이터가 하나라도 있으면 배치에 포함
            if (!advices.isEmpty() || !resources.isEmpty() || !goals.isEmpty()) {
                nodeTextsMap.put(key, new TextFieldIntegrationService.NodeTextData(
                        advices, resources, goals
                ));
            } else {
                // 빈 데이터는 AI 호출 없이 빈 응답으로 처리
                emptyResponseMap.put(key, new TextFieldIntegrationResponse(null, null, null));
            }
        });

        // 배치로 텍스트 통합 (데이터가 있는 노드만)
        Map<String, TextFieldIntegrationResponse> integratedTextsMap = new HashMap<>();

        if (!nodeTextsMap.isEmpty()) {
            try {
                log.info("배치 텍스트 통합 시작: {}개 노드 (빈 데이터 {}개 제외)",
                        nodeTextsMap.size(), emptyResponseMap.size());
                integratedTextsMap.putAll(textFieldIntegrationService.integrateBatch(nodeTextsMap));
                log.info("배치 텍스트 통합 완료");
            } catch (Exception e) {
                log.error("배치 텍스트 통합 실패, 모든 노드에 빈 응답 적용: {}", e.getMessage(), e);
                // AI 호출 실패해도 통합은 계속 진행 (빈 응답으로 대체)
                nodeTextsMap.keySet().forEach(key ->
                        integratedTextsMap.put(key, new TextFieldIntegrationResponse(null, null, null))
                );
            }
        } else {
            log.info("텍스트 데이터가 없어 AI 호출 생략 (모든 노드 빈 데이터)");
        }

        // 빈 응답 병합
        integratedTextsMap.putAll(emptyResponseMap);

        return integratedTextsMap;
    }

    /**
     * 통합된 텍스트 필드와 집계 데이터로 노드를 생성합니다.
     *
     * @param aggregation 집계 결과
     * @param taskMap Task ID → Task 매핑
     * @param integratedTextsMap 통합된 텍스트 필드
     * @return key: 노드 키, value: 생성된 노드
     */
    private Map<String, RoadmapNode> buildNodesFromIntegratedTexts(
            RoadmapAggregator.AggregationResult aggregation,
            Map<Long, Task> taskMap,
            Map<String, TextFieldIntegrationResponse> integratedTextsMap
    ) {
        Map<String, RoadmapNode> keyToNode = new HashMap<>();

        aggregation.agg.forEach((key, aggNode) -> {
            Double avgDifficulty = calculateAverage(aggregation.descriptions.difficulties.get(key));
            Double avgImportance = calculateAverage(aggregation.descriptions.importances.get(key));
            Integer avgEstimatedHours = calculateIntegerAverage(aggregation.descriptions.estimatedHours.get(key));

            TextFieldIntegrationResponse integratedTexts = integratedTextsMap.get(key);

            RoadmapNode node = RoadmapNode.builder()
                    .taskName(aggNode.displayName)
                    .learningAdvice(integratedTexts != null ? integratedTexts.learningAdvice() : null)
                    .recommendedResources(integratedTexts != null ? integratedTexts.recommendedResources() : null)
                    .learningGoals(integratedTexts != null ? integratedTexts.learningGoals() : null)
                    .difficulty(avgDifficulty != null ? (int) Math.round(avgDifficulty) : null)
                    .importance(avgImportance != null ? (int) Math.round(avgImportance) : null)
                    .estimatedHours(avgEstimatedHours)
                    .task(aggNode.task != null ? taskMap.get(aggNode.task.getId()) : null)
                    .roadmapId(0L) // 임시 값
                    .roadmapType(RoadmapNode.RoadmapType.JOB)
                    .build();
            keyToNode.put(key, node);
        });

        return keyToNode;
    }

    private ParentEvaluation evaluateParentCandidates(RoadmapAggregator.AggregationResult aggregation) {
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
                    // NPE 방어: parentKey가 agg에 없는 경우 처리 (데이터 정합성 오류)
                    RoadmapAggregator.AggregatedNode parentAggNode = aggregation.agg.get(parentKey);
                    if (parentAggNode == null) {
                        log.warn("데이터 정합성 오류: parentKey={}가 집계 데이터에 없음, childKey={} skip",
                                parentKey, childKey);
                        continue;
                    }
                    double ratio = (double) transitionCount / parentAggNode.count;
                    if (ratio >= BRANCH_THRESHOLD) {
                        chosen.add(childKey);
                    }
                }
                double priorityScore = calculatePriorityScore(parentKey, childKey, transitionCount, parentTotalTransitions, aggregation);
                childToParentCandidates.computeIfAbsent(childKey, k -> new ArrayList<>())
                        .add(new ParentCandidate(parentKey, transitionCount, priorityScore));
            }
            if (!chosen.isEmpty()) {
                chosenChildren.put(parentKey, chosen);
            }
        }
        Map<String, String> childToBestParent = new HashMap<>();
        childToParentCandidates.forEach((child, candidates) -> {
            candidates.sort(Comparator.comparingDouble(ParentCandidate::priorityScore).reversed().thenComparing(ParentCandidate::parentKey));
            childToBestParent.put(child, candidates.get(0).parentKey);
        });
        return new ParentEvaluation(chosenChildren, childToBestParent);
    }

    private double calculatePriorityScore(String parentKey, String childKey, int transitionCount, int parentTotalTransitions, RoadmapAggregator.AggregationResult aggregation) {
        double transitionPopularity = (double) transitionCount / aggregation.totalMentorCount;
        double transitionStrength = (double) transitionCount / parentTotalTransitions;

        double avgParentPos = aggregation.positions.getOrDefault(parentKey, Collections.emptyList()).stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double avgChildPos = aggregation.positions.getOrDefault(childKey, Collections.emptyList()).stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double positionSimilarity = 1.0 / (1.0 + Math.abs(avgParentPos - avgChildPos));

        double mentorCoverage = (double) aggregation.mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size() / aggregation.totalMentorCount;

        return W_TRANSITION_POPULARITY * transitionPopularity +
                W_TRANSITION_STRENGTH * transitionStrength +
                W_POSITION_SIMILARITY * positionSimilarity +
                W_MENTOR_COVERAGE * mentorCoverage;
    }

    private TreeBuildResult constructTreeViaBFS(String rootKey, Map<String, RoadmapNode> keyToNode, ParentEvaluation parentEval, RoadmapAggregator.AggregationResult aggregation) {
        TreeBuildResult result = new TreeBuildResult(rootKey, keyToNode);
        Queue<DeferredChild> deferredQueue = new ArrayDeque<>();
        performFirstPassBFS(result, parentEval, aggregation, deferredQueue);
        performDeferredRetry(result, parentEval, deferredQueue);
        initializeMainRoot(result);
        return result;
    }

    private void performFirstPassBFS(TreeBuildResult result, ParentEvaluation parentEval, RoadmapAggregator.AggregationResult aggregation, Queue<DeferredChild> deferredQueue) {
        Queue<String> q = new ArrayDeque<>();
        q.add(result.rootKey);
        while (!q.isEmpty()) {
            String pk = q.poll();
            RoadmapNode parentNode = result.keyToNode.get(pk);

            // NPE 방어: parentNode가 없는 경우 처리
            if (parentNode == null) {
                log.warn("부모 노드가 keyToNode 맵에 없음: parentKey={}, skip", pk);
                continue;
            }

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

    private void performDeferredRetry(TreeBuildResult result, ParentEvaluation parentEval, Queue<DeferredChild> deferredQueue) {
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

    private void recordAsAlternative(String parentKey, String childKey, RoadmapAggregator.AggregationResult aggregation, TreeBuildResult result) {
        int transitionCount = aggregation.transitions.getOrDefault(parentKey, Collections.emptyMap()).getOrDefault(childKey, 0);
        int parentMentorCount = aggregation.mentorAppearSet.getOrDefault(parentKey, Collections.emptySet()).size();
        double transitionPopularity = (double) transitionCount / aggregation.totalMentorCount;
        List<Integer> parentPosList = aggregation.positions.getOrDefault(parentKey, Collections.emptyList());
        double avgParentPos = parentPosList.isEmpty() ? 99.0 : parentPosList.stream().mapToInt(Integer::intValue).average().orElse(99.0);
        double positionScore = 1.0 / (avgParentPos + 1);
        double mentorCoverageScore = (double) parentMentorCount / aggregation.totalMentorCount;
        double score = W_TRANSITION_POPULARITY * transitionPopularity + W_POSITION_SIMILARITY * positionScore + W_MENTOR_COVERAGE * mentorCoverageScore;
        AlternativeParentInfo info = new AlternativeParentInfo(parentKey, transitionCount, parentMentorCount, score);
        result.skippedParents.computeIfAbsent(childKey, k -> new ArrayList<>()).add(info);
    }

    // --- 헬퍼 메서드 ---
    private Double calculateAverage(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private Integer calculateIntegerAverage(List<Integer> list) {
        Double avg = calculateAverage(list);
        return avg != null ? (int) Math.round(avg) : null;
    }

    // --- 트리 구성 DTOs ---
    @Getter
    public static class TreeBuildResult {
        public final String rootKey;
        public final Map<String, RoadmapNode> keyToNode;
        public final Set<String> visited = new HashSet<>();
        public final Map<String, List<AlternativeParentInfo>> skippedParents = new HashMap<>();

        public TreeBuildResult(String rootKey, Map<String, RoadmapNode> keyToNode) {
            this.rootKey = rootKey;
            this.keyToNode = keyToNode;
            visited.add(rootKey);
        }
    }

    @Getter
    private static class ParentEvaluation {
        final Map<String, List<String>> chosenChildren;
        final Map<String, String> childToBestParent;
        ParentEvaluation(Map<String, List<String>> chosenChildren, Map<String, String> childToBestParent) {
            this.chosenChildren = chosenChildren;
            this.childToBestParent = childToBestParent;
        }
    }

    public record AlternativeParentInfo(String parentKey, int transitionCount, int mentorCount, double score) {}
    private record ParentCandidate(String parentKey, int transitionCount, double priorityScore) {}
    private record DeferredChild(String parentKey, String childKey, int retryCount) {}
}