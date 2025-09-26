package com.back.domain.roadmap.task.dto;

import jakarta.validation.constraints.NotNull;

public record LinkPendingAliasRequest(
        @NotNull(message = "Task ID는 필수입니다.")
        Long taskId
) {
}
