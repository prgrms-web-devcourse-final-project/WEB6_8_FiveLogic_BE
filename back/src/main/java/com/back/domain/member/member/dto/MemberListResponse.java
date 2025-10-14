package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

import java.time.LocalDateTime;

public record MemberListResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Member.Role role,
        Boolean isDeleted,
        LocalDateTime createdAt
) {
    public static MemberListResponse from(Member member) {
        return new MemberListResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getRole(),
                member.getIsDeleted(),
                member.getCreateDate()
        );
    }
}
