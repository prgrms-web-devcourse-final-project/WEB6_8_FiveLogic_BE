package com.back.domain.mentoring.mentoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ReviewRequest(
    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Schema(description = "별점 (0.0 ~ 5.0)", example = "4.5")
    Double rating,

    @Size(max = 1000)
    @Schema(description = "리뷰 내용", example = "review content")
    String content
) {
}
