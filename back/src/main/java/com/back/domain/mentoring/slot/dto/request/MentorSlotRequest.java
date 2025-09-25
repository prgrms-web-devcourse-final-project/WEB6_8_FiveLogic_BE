package com.back.domain.mentoring.slot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MentorSlotRequest(
    @Schema(description = "멘토 ID")
    @NotNull
    Long mentorId,

    @Schema(description = "시작 일시")
    @NotNull
    LocalDateTime startDateTime,

    @Schema(description = "종료 일시")
    @NotNull
    LocalDateTime endDateTime
) {
}
