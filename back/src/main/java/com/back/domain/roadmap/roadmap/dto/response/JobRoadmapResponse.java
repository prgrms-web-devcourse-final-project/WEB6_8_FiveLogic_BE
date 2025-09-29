package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;

import java.time.LocalDateTime;
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

    // 정적 팩터리 메서드 - JobRoadmap과 Job 정보로부터 Response DTO 생성
    public static JobRoadmapResponse from(JobRoadmap jobRoadmap, String jobName) {
        // 모든 노드를 Map으로 변환 (트리 구성용)
        Map<Long, JobRoadmapNodeResponse> nodeMap = jobRoadmap.getNodes().stream()
                .collect(Collectors.toMap(
                    node -> node.getId(),
                    JobRoadmapNodeResponse::from
                ));

        // 트리 구조 구성: 자식 노드들을 부모에 연결
        jobRoadmap.getNodes().forEach(node -> {
            if (node.getParent() != null) {
                JobRoadmapNodeResponse parentNode = nodeMap.get(node.getParent().getId());
                JobRoadmapNodeResponse childNode = nodeMap.get(node.getId());
                if (parentNode != null && childNode != null) {
                    parentNode.addChild(childNode);
                }
            }
        });

        // 루트 노드들만 반환 (자식 노드들은 children 필드에 포함되어 전체 트리 구조 제공)
        List<JobRoadmapNodeResponse> nodes = jobRoadmap.getNodes().stream()
                .filter(node -> node.getParent() == null)
                .map(node -> nodeMap.get(node.getId()))
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
}