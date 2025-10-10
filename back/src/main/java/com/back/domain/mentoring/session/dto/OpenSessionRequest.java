package com.back.domain.mentoring.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OpenSessionRequest(
        @Schema(description = "세션 ID")
        Long sessionId
) {
}
