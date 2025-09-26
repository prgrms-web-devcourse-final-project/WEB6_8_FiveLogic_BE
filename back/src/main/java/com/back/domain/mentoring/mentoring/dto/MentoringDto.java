package com.back.domain.mentoring.mentoring.dto;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MentoringDto(
    @Schema(description = "멘토링 ID")
    Long mentoringId,
    @Schema(description = "멘토링 제목")
    String title,
    @Schema(description = "멘토링 태그")
    List<String> tags
) {
    public static MentoringDto from(Mentoring mentoring) {
        return new MentoringDto(
            mentoring.getId(),
            mentoring.getTitle(),
            mentoring.getTags()
        );
    }
}
