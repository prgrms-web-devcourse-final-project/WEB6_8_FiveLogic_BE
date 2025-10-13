package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

public record MemberMeResponse(
    Long memberId,
    String publicId,
    String email,
    String name,
    String nickname,
    String role,
    Long mentorId,
    Long menteeId
) {
    public static MemberMeResponse of(Member member, Long mentorId, Long menteeId) {
        return new MemberMeResponse(
            member.getId(),
            member.getPublicId(),
            member.getEmail(),
            member.getName(),
            member.getNickname(),
            member.getRole().name(),
            mentorId,
            menteeId
        );
    }
}
