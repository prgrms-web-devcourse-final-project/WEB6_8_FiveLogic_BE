package com.back.domain.member.member.dto;

public record MentorUpdateRequest(
        String nickname,
        String career,
        Integer careerYears
) {
}