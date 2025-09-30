package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record JobRoadmapNodeResponse(
    Long id,
    Long parentId,       // 부모 노드 ID (null이면 루트 노드)
    List<Long> childIds, // 자식 노드 ID 목록 (프론트엔드 렌더링용)
    Long taskId,         // Task와 연결된 경우의 표준 Task ID
    String taskName,     // 표시용 Task 이름
    String description,
    int stepOrder,
    int level,           // 트리 깊이 (0: 루트, 1: 1단계 자식...)
    boolean isLinkedToTask,
    Double weight,       // 이 노드의 가중치 (JobRoadmapNodeStat에서)

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<JobRoadmapNodeResponse> children
) {

    // 정적 팩토리 메서드 - RoadmapNode로부터 Response DTO 생성 (자식 노드 정보 포함)
    public static JobRoadmapNodeResponse from(RoadmapNode node, List<JobRoadmapNodeResponse> children) {
        List<Long> childIds = children != null ?
            children.stream().map(JobRoadmapNodeResponse::id).toList() :
            List.of();

        return new JobRoadmapNodeResponse(
            node.getId(),
            node.getParent() != null ? node.getParent().getId() : null,
            childIds,
            node.getTask() != null ? node.getTask().getId() : null,
            node.getTask() != null ? node.getTask().getName() : node.getTaskName(),
            node.getDescription(),
            node.getStepOrder(),
            node.getLevel(),
            node.getTask() != null,
            null, // weight는 서비스에서 별도로 설정
            children != null ? children : List.of()
        );
    }

    // 가중치 설정 헬퍼 메서드 (불변 객체이므로 새 인스턴스 반환)
    public JobRoadmapNodeResponse withWeight(Double weight) {
        return new JobRoadmapNodeResponse(
            this.id, this.parentId, this.childIds, this.taskId, this.taskName, this.description,
            this.stepOrder, this.level, this.isLinkedToTask, weight, this.children
        );
    }
}