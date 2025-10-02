package com.back.domain.mentoring.mentoring.dto.response;

import com.back.domain.mentoring.mentoring.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReviewResponse(
    @Schema(description = "리뷰 ID")
    Long reviewId,
    @Schema(description = "별점 (0.0 ~ 5.0)")
    Double rating,
    @Schema(description = "리뷰 내용")
    String content,
    @Schema(description = "생성일")
    LocalDateTime createDate,
    @Schema(description = "수정일")
    LocalDateTime modifyDate,

    @Schema(description = "멘티 ID")
    Long menteeId,
    @Schema(description = "멘티 닉네임")
    String menteeNickname
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getRating(),
            review.getContent(),
            review.getCreateDate(),
            review.getModifyDate(),
            review.getMentee().getId(),
            review.getMentee().getMember().getNickname()
        );
    }
}
