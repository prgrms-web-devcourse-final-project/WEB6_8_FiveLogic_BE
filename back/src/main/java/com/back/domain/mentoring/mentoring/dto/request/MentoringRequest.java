package com.back.domain.mentoring.mentoring.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MentoringRequest(
    @Schema(description = "멘토링 제목", example = "title")
    @NotNull @Size(max = 100)
    String title,

    @Schema(description = "멘토링 태그", example = "[\"Java\", \"Spring\"]")
    List<String> tags,

    @Schema(description = "멘토링 소개", example = "bio")
    @NotNull
    String bio
) {
}
