package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;

public record MentorMyPageResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Long jobId,
        Double rate,
        Integer careerYears
) {
    public static MentorMyPageResponse from(Member member, Mentor mentor) {
        return new MentorMyPageResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                mentor.getJobId(),
                mentor.getRate(),
                mentor.getCareerYears()
        );
    }
}