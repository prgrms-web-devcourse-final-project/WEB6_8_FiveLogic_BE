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
    @Schema(description = "멘토링 평점")
    Double rating,
    @Schema(description = "생성일")
    LocalDateTime createDate,
    @Schema(description = "수정일")
    LocalDateTime modifyDate
) {
    public static MentoringDetailDto from(Mentoring mentoring) {
        return new MentoringDetailDto(
            mentoring.getId(),
            mentoring.getTitle(),
            mentoring.getTagNames(),
            mentoring.getBio(),
            mentoring.getThumb(),
            mentoring.getRating(),
            mentoring.getCreateDate(),
            mentoring.getModifyDate()
        );
    }
}
