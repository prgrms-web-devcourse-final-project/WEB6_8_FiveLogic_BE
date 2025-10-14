package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;

public record MenteeMyPageResponse(
        Long id,
        String email,
        String name,
        String nickname,
        String interestedField
) {
    public static MenteeMyPageResponse from(Member member, Mentee mentee) {
        return new MenteeMyPageResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                mentee.getJob().getName()
        );
    }
}