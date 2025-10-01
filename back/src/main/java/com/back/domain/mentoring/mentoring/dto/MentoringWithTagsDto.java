package com.back.domain.mentoring.mentoring.dto;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MentoringWithTagsDto(
    @Schema(description = "멘토링 ID")
    Long mentoringId,
    @Schema(description = "멘토링 제목")
    String title,
    @Schema(description = "멘토링 태그")
    List<String> tags,
    @Schema(description = "멘토 ID")
    Long mentorId,
    @Schema(description = "멘토 닉네임")
    String nickname
) {
    public static MentoringWithTagsDto from(Mentoring mentoring) {
        return new MentoringWithTagsDto(
            mentoring.getId(),
            mentoring.getTitle(),
            mentoring.getTags(),
            mentoring.getMentor().getId(),
            mentoring.getMentor().getMember().getNickname()
        );
    }
}
