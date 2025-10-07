package com.back.domain.mentoring.session.dto;

public record GetSessionInfoResponse(
        String mentoringTitle,
        String mentorName,
        String menteeName,
        String sessionStatus
) {
}
