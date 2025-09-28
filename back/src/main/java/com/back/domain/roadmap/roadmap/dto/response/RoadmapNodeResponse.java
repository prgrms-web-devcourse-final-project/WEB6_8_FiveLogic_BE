package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;

public record RoadmapNodeResponse(
    Long id,
    Long taskId,         // Task와 연결된 경우의 표준 Task ID
    String taskName,     // 표시용 Task 이름(Task와 연결된 경우 해당 Task 이름, 자유 입력시 입력값)
    String description,
    int stepOrder,
    boolean isLinkedToTask // Task와 연결 여부
) {

    // 정적 팩터리 메서드 - RoadmapNode로부터 Response DTO 생성
    public static RoadmapNodeResponse from(RoadmapNode node) {
        return new RoadmapNodeResponse(
            node.getId(),
            node.getTask() != null ? node.getTask().getId() : null,
            node.getTaskName(), // taskName 필드 직접 사용 (Task 엔티티 접근 불필요)
            node.getDescription(),
            node.getStepOrder(),
            node.getTask() != null
        );
    }
}