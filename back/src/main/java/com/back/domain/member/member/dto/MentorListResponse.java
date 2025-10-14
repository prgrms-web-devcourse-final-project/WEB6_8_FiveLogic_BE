package com.back.domain.member.member.dto;

import com.back.domain.member.mentor.entity.Mentor;

public record MentorListResponse(
        Long mentorId,
        Long memberId,
        String name,
        String nickname,
        String job,
        Integer careerYears,
        Double rate
) {
    public static MentorListResponse from(Mentor mentor) {
        return new MentorListResponse(
                mentor.getId(),
                mentor.getMember().getId(),
                mentor.getMember().getName(),
                mentor.getMember().getNickname(),
                mentor.getJob() != null ? mentor.getJob().getName() : null,
                mentor.getCareerYears(),
                mentor.getRate()
        );
    }
}
