package com.back.domain.member.member.dto;

public record MentorSignupVerifyRequest(
    String email,
    String verificationCode,
    String password,
    String name,
    String career,
    Integer careerYears
) {
}