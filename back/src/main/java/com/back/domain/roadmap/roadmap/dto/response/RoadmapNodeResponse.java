package com.back.domain.roadmap.roadmap.dto.response;

// 순수 데이터 전송 객체 - 엔티티에 의존하지 않음
public record RoadmapNodeResponse(
    Long id,
    Long taskId,         // Task와 연결된 경우의 표준 Task ID
    String taskName,     // 표시용 Task 이름
    String standardTaskName,  // 표준 Task 이름 (연결된 경우만)
    String description,
    int stepOrder,
    boolean isLinkedToTask // Task와 연결 여부
) {}