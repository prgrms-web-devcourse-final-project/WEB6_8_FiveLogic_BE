package com.back.domain.member.member.dto;

public record MentorSignupRequest(
    String email,
    String password,
    String name,
    String career,
    Integer careerYears
) {
}