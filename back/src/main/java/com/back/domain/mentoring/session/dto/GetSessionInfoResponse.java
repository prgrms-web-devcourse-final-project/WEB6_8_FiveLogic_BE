package com.back.domain.mentoring.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetSessionInfoResponse(
        @Schema(description = "멘토링 제목")
        String mentoringTitle,
        @Schema(description = "멘토 이름")
        String mentorName,
        @Schema(description = "멘티 이름")
        String menteeName,
        @Schema(description = "세션 상태")
        String sessionStatus
) {
}
