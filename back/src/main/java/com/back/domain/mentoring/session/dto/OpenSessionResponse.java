package com.back.domain.mentoring.session.dto;

public record OpenSessionResponse(
        String sessionUrl,
        String mentoringTitle,
        String status
) {
}
