package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.task.entity.Task;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 멘토 로드맵 목록을 기반으로 통계를 집계하는 역할을 담당합니다.
 *  * 이 클래스는 데이터베이스 의존성이 없으며, 순수한 데이터 처리 로직만 포함합니다.
 */
@Component
public class RoadmapAggregator {

    public AggregationResult aggregate(List<MentorRoadmap> mentorRoadmaps) {
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
        result.agg.computeIfAbsent(key, k -> new AggregatedNode(
                rn.getTask(),
                rn.getTask() != null ? rn.getTask().getName() : rn.getTaskName()
        )).count++;

        result.positions.computeIfAbsent(key, k -> new ArrayList<>()).add(position);
        result.mentorAppearSet.computeIfAbsent(key, k -> new HashSet<>()).add(mentorId);
    }

    private void aggregateDescriptions(RoadmapNode rn, String key, DescriptionCollections descriptions) {
        if (rn.getLearningAdvice() != null && !rn.getLearningAdvice().isBlank()) {
            descriptions.learningAdvices.computeIfAbsent(key, k -> new ArrayList<>()).add(rn.getLearningAdvice());
        }
        if (rn.getRecommendedResources() != null && !rn.getRecommendedResources().isBlank()) {
            descriptions.recommendedResources.computeIfAbsent(key, k -> new ArrayList<>()).add(rn.getRecommendedResources());
        }
        if (rn.getLearningGoals() != null && !rn.getLearningGoals().isBlank()) {
            descriptions.learningGoals.computeIfAbsent(key, k -> new ArrayList<>()).add(rn.getLearningGoals());
        }
        if (rn.getDifficulty() != null) {
            descriptions.difficulties.computeIfAbsent(key, k -> new ArrayList<>()).add(rn.getDifficulty());
        }
        if (rn.getImportance() != null) {
            descriptions.importances.computeIfAbsent(key, k -> new ArrayList<>()).add(rn.getImportance());
        }
        if (rn.getEstimatedHours() != null) {
            descriptions.estimatedHours.computeIfAbsent(key, k -> new ArrayList<>()).add(rn.getEstimatedHours());
        }
    }

    private void aggregateTransition(String fromKey, String toKey, AggregationResult result) {
        result.transitions.computeIfAbsent(fromKey, k -> new HashMap<>()).merge(toKey, 1, Integer::sum);
    }

    public static String generateKey(RoadmapNode rn) {
        if (rn.getTask() != null) {
            return "T:" + rn.getTask().getId();
        }
        String name = rn.getTaskName();
        if (name == null || name.trim().isEmpty()) return "N:__unknown__";
        return "N:" + name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    // --- 통계 DTOs ---

    @Getter
    public static class AggregationResult {
        public final Map<String, AggregatedNode> agg = new HashMap<>();
        public final Map<String, Map<String, Integer>> transitions = new HashMap<>();
        public final Map<String, Integer> rootCount = new HashMap<>();
        public final Map<String, Set<Long>> mentorAppearSet = new HashMap<>();
        public final Map<String, List<Integer>> positions = new HashMap<>();
        public final DescriptionCollections descriptions = new DescriptionCollections();
        public final int totalMentorCount;

        public AggregationResult(int totalMentorCount) {
            this.totalMentorCount = totalMentorCount;
        }
    }

    @Getter
    public static class DescriptionCollections {
        public final Map<String, List<String>> learningAdvices = new HashMap<>();
        public final Map<String, List<String>> recommendedResources = new HashMap<>();
        public final Map<String, List<String>> learningGoals = new HashMap<>();
        public final Map<String, List<Integer>> difficulties = new HashMap<>();
        public final Map<String, List<Integer>> importances = new HashMap<>();
        public final Map<String, List<Integer>> estimatedHours = new HashMap<>();
    }

    public static class AggregatedNode {
        public Task task;
        public String displayName;
        public int count = 0;

        public AggregatedNode(Task task, String displayName) {
            this.task = task;
            this.displayName = displayName;
        }
    }

}
