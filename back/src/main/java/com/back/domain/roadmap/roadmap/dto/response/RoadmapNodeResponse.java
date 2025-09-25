package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import lombok.Getter;

@Getter
public class RoadmapNodeResponse {
    private final Long id;
    private final Long taskId;         // Task와 연결된 경우의 표준 Task ID
    private final String taskName;     // 표시용 Task 이름
    private final String standardTaskName;  // 표준 Task 이름 (연결된 경우만)
    private final String description;
    private final int stepOrder;
    private final boolean isLinkedToTask; // Task와 연결 여부

    public RoadmapNodeResponse(RoadmapNode node) {
        this.id = node.getId();
        this.taskId = node.getTask() != null ? node.getTask().getId() : null;
        this.taskName = node.getRawTaskName();
        this.standardTaskName = node.getTask() != null ? node.getTask().getName() : null;
        this.description = node.getDescription();
        this.stepOrder = node.getStepOrder();
        this.isLinkedToTask = node.getTask() != null;
    }
}