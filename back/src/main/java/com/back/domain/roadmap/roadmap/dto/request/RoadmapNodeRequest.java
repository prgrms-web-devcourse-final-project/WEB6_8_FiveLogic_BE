package com.back.domain.roadmap.roadmap.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoadmapNodeRequest(
        Long taskId,  // nullable - Task와 연결되지 않은 경우 null

        @NotBlank(message = "Task 이름은 필수입니다.")
        @Size(max = 100, message = "Task 이름은 100자를 초과할 수 없습니다.")
        String taskName,  // 표시용 Task 이름 (rawTaskName으로 저장)

        @NotBlank(message = "노드 설명은 필수입니다.")
        @Size(max = 2000, message = "노드 설명은 2000자를 초과할 수 없습니다.")
        String description,

        @Min(value = 1, message = "단계 순서는 1 이상이어야 합니다.")
        int stepOrder
) {}