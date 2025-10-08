package com.back.domain.mentoring.session.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        String senderName,
        String content,
        LocalDateTime createdAt
) {
}
