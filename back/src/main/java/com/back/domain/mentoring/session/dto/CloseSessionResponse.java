package com.back.domain.mentoring.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CloseSessionResponse(
        @Schema(description = "세션 URL")
        String sessionUrl,
        @Schema(description = "멘토링 제목")
        String mentoringTitle,
        @Schema(description = "세션 상태")
        String status
) {
}
