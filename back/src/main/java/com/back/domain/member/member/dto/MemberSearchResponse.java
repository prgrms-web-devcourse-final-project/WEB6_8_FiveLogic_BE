package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;

import java.time.LocalDateTime;

public record MemberSearchResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Member.Role role,
        Boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String career,        // 멘토인 경우에만 값이 있음 (TODO: Job 연결 후 수정 예정)
        Integer careerYears,  // 멘토인 경우에만 값이 있음
        String interestedField  // 멘티인 경우에만 값이 있음 (TODO: Job 연결 후 수정 예정)
) {
    public static MemberSearchResponse from(Member member, Mentor mentor, Mentee mentee) {
        return new MemberSearchResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getRole(),
                member.getIsDeleted(),
                member.getCreateDate(),
                member.getModifyDate(),
                mentor != null ? "TODO: Job 연결 필요" : null,  // TODO: Job 연결 후 수정
                mentor != null ? mentor.getCareerYears() : null,
                mentee != null ? "TODO: Job 연결 필요" : null   // TODO: Job 연결 후 수정
        );
    }
}