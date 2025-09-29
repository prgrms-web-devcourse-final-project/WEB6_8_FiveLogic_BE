package com.back.domain.member.mentee.dto;

import com.back.domain.member.mentee.entity.Mentee;
import io.swagger.v3.oas.annotations.media.Schema;

public record MenteeDto(
    @Schema(description = "멘티 ID")
    Long menteeId,
    @Schema(description = "멘티 닉네임")
    String nickname
) {
    public static MenteeDto from(Mentee mentee) {
        return new MenteeDto(
            mentee.getId(),
            mentee.getMember().getNickname()
        );
    }
}
