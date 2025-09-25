package com.back.domain.mentoring.mentoring.dto;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MentoringDetailDto(
    @Schema(description = "멘토링 ID")
    Long mentoringId,
    @Schema(description = "멘토링 제목")
    String title,
    @Schema(description = "멘토링 태그")
    List<String> tags,
    @Schema(description = "멘토링 소개")
    String bio,
    @Schema(description = "멘토링 썸네일")
    String thumb,
    @Schema(description = "생성일")
    LocalDateTime createDate,
    @Schema(description = "수정일")
    LocalDateTime modifyDate
) {
    public static MentoringDetailDto from(Mentoring mentoring) {
        return new MentoringDetailDto(
            mentoring.getId(),
            mentoring.getTitle(),
            mentoring.getTags(),
            mentoring.getBio(),
            mentoring.getThumb(),
            mentoring.getCreateDate(),
            mentoring.getModifyDate()
        );
    }
}
