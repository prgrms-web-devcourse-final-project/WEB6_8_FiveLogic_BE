package com.back.domain.member.member.dto;

public record MenteeSignupRequest(
    String email,
    String password,
    String name,
    String nickname,
    String interestedField
) {
}