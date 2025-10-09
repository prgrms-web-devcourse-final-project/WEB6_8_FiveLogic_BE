package com.back.domain.mentoring.session.dto;

import com.back.domain.mentoring.session.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

public record ChatMessageRequest(
        @Schema(description = "메시지 타입", example = "TEXT")
        MessageType type,
        @Schema(description = "메시지 내용", example = "msg")
        String content
) {
}
