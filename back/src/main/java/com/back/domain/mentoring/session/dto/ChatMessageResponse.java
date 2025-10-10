package com.back.domain.mentoring.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        @Schema(description = "작성자명")
        String senderName,
        @Schema(description = "메시지 내용")
        String content,
        @Schema(description = "작성일시")
        LocalDateTime createdAt
) {
}
