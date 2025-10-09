package com.back.domain.mentoring.session.dto;

public record CloseSessionResponse(
        String sessionUrl,
        String mentoringTitle,
        String status
) {
}
