package com.back.domain.member.member.dto;

public record MenteeUpdateRequest(
        String nickname,
        String interestedField
) {
}