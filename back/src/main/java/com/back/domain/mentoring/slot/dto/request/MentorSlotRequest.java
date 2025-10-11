package com.back.domain.mentoring.slot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MentorSlotRequest(
    @Schema(description = "멘토 ID")
    @NotNull
    Long mentorId,

    @Schema(description = "시작 일시", example = "yyyy-MM-ddTHH:mm")
    @NotNull
    LocalDateTime startDateTime,

    @Schema(description = "종료 일시", example = "yyyy-MM-ddTHH:mm")
    @NotNull
    LocalDateTime endDateTime
) {
}
