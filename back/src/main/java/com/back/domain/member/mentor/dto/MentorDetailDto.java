package com.back.domain.member.mentor.dto;

import com.back.domain.member.mentor.entity.Mentor;
import io.swagger.v3.oas.annotations.media.Schema;

public record MentorDetailDto(
    @Schema(description = "멘토 ID")
    Long mentorId,
    @Schema(description = "멘토 회원 ID")
    Long mentorMemberId,
    @Schema(description = "멘토 닉네임")
    String nickname,
    @Schema(description = "평점")
    Double rate,
    // TODO: Job id, name
    @Schema(description = "연차")
    Integer careerYears
) {
    public static MentorDetailDto from(Mentor mentor) {
        return new MentorDetailDto(
            mentor.getId(),
            mentor.getMember().getId(),
            mentor.getMember().getNickname(),
            mentor.getRate(),
            mentor.getCareerYears()
        );
    }
}
