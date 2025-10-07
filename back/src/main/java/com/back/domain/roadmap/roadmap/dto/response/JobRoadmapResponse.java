package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record JobRoadmapResponse(
    Long id,
    Long jobId,
    String jobName,
    List<JobRoadmapNodeResponse> nodes,
    int totalNodeCount,
    LocalDateTime createdDate,
    LocalDateTime modifiedDate
) {

    // 정적 팩터리 메서드 - JobRoadmap과 Job 정보, 통계 정보로부터 Response DTO 생성
    public static JobRoadmapResponse from(JobRoadmap jobRoadmap, String jobName, Map<Long, JobRoadmapNodeStat> statMap) {
        // 부모-자식 관계 맵 생성
        Map<Long, List<RoadmapNode>> childrenMap = jobRoadmap.getNodes().stream()
                .filter(node -> node.getParent() != null)
                .collect(Collectors.groupingBy(node -> node.getParent().getId()));

        // 노드를 재귀적으로 변환하는 함수
        Map<Long, JobRoadmapNodeResponse> nodeResponseMap = new HashMap<>();

        // 모든 노드를 bottom-up 방식으로 변환 (자식부터 부모 순서, 통계 정보 포함)
        buildNodeResponses(jobRoadmap.getNodes(), childrenMap, nodeResponseMap, statMap);

        // 루트 노드들만 반환 (자식 노드들은 children 필드에 포함되어 전체 트리 구조 제공)
        List<JobRoadmapNodeResponse> nodes = jobRoadmap.getNodes().stream()
                .filter(node -> node.getParent() == null)
                .map(node -> nodeResponseMap.get(node.getId()))
                .sorted((a, b) -> {
                    int levelCompare = Integer.compare(a.level(), b.level());
                    return levelCompare != 0 ? levelCompare : Integer.compare(a.stepOrder(), b.stepOrder());
                })
                .toList();

        return new JobRoadmapResponse(
            jobRoadmap.getId(),
            jobRoadmap.getJob().getId(),
            jobName,
            nodes,
            jobRoadmap.getNodes().size(),
            jobRoadmap.getCreateDate(),
            jobRoadmap.getModifyDate()
        );
    }

    // 노드 응답 객체들을 재귀적으로 구성하는 헬퍼 메서드 (통계 정보 포함)
    private static void buildNodeResponses(
            List<RoadmapNode> allNodes,
            Map<Long, List<RoadmapNode>> childrenMap,
            Map<Long, JobRoadmapNodeResponse> nodeResponseMap,
            Map<Long, JobRoadmapNodeStat> statMap) {

        // 노드들을 level 역순으로 정렬 (깊은 노드부터 처리)
        List<RoadmapNode> sortedNodes = allNodes.stream()
                .sorted((a, b) -> Integer.compare(b.getLevel(), a.getLevel()))
                .toList();

        for (RoadmapNode node : sortedNodes) {
            // 자식 노드들의 응답 객체 가져오기
            List<JobRoadmapNodeResponse> childResponses = childrenMap
                    .getOrDefault(node.getId(), List.of())
                    .stream()
                    .map(child -> nodeResponseMap.get(child.getId()))
                    .filter(response -> response != null)
                    .sorted((a, b) -> Integer.compare(a.stepOrder(), b.stepOrder()))
                    .toList();

            // 현재 노드의 응답 객체 생성
            JobRoadmapNodeResponse nodeResponse = JobRoadmapNodeResponse.from(node, childResponses);

            // 통계 정보가 있으면 추가
            JobRoadmapNodeStat stat = statMap.get(node.getId());
            if (stat != null) {
                nodeResponse = nodeResponse
                        .withWeight(stat.getWeight())
                        .withStats(stat.getMentorCount(), stat.getTotalMentorCount(), stat.getMentorCoverageRatio());
            }

            nodeResponseMap.put(node.getId(), nodeResponse);
        }
    }
}