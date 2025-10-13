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
        String job,           // 멘토/멘티 모두: 직업명 또는 관심분야
        Integer careerYears,  // 멘토인 경우에만 값이 있음
        Long mentorId,        // 멘토 ID (멘토인 경우에만 값이 있음)
        Long menteeId         // 멘티 ID (멘티인 경우에만 값이 있음)
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
                mentor != null ? mentor.getJob().getName() : (mentee != null ? mentee.getJob().getName() : null),
                mentor != null ? mentor.getCareerYears() : null,
                mentor != null ? mentor.getId() : null,
                mentee != null ? mentee.getId() : null
        );
    }
}