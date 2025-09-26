package com.back.domain.roadmap.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePendingAliasRequest(
        @NotBlank(message = "기술명은 필수입니다")
        @Size(min = 1, max = 50, message = "기술명은 1-50자 사이여야 합니다")
        String taskName
) {}
