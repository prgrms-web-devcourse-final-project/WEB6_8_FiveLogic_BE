package com.back.domain.mentoring.session.dto;

public record WebRtcSignalingMessage(
        String type,
        String from,
        String to,
        String sessionId,
        Object payload
) {
}
