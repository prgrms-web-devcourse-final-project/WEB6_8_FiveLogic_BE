package com.back.domain.member.mentor.dto;

import com.back.domain.member.mentor.entity.Mentor;
import io.swagger.v3.oas.annotations.media.Schema;

public record MentorDto(
    @Schema(description = "멘토 ID")
    Long mentorId,
    @Schema(description = "멘토 회원 ID")
    Long mentorMemberId,
    @Schema(description = "멘토 닉네임")
    String nickname
) {
    public static MentorDto from(Mentor mentor) {
        return new MentorDto(
            mentor.getId(),
            mentor.getMember().getId(),
            mentor.getMember().getNickname()
        );
    }
}
