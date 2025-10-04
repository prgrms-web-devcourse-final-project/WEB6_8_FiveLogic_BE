package com.back.domain.mentoring.session.dto;

import com.back.domain.mentoring.session.entity.MessageType;
import com.back.domain.mentoring.session.entity.SenderRole;

public record ChatMessageRequest(
        MessageType type,
        String content
) {
}
