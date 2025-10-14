package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;

public record MentorMyPageResponse(
        Long id,
        String email,
        String name,
        String nickname,
        String job,
        Double rate,
        Integer careerYears
) {
    public static MentorMyPageResponse from(Member member, Mentor mentor) {
        return new MentorMyPageResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                mentor.getJob().getName(),
                mentor.getRate(),
                mentor.getCareerYears()
        );
    }
}