package com.back.domain.mentoring.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
    @Schema(description = "멘토 ID")
    @NotNull
    Long mentorId,

    @Schema(description = "멘토 슬롯 ID")
    @NotNull
    Long mentorSlotId,

    @Schema(description = "멘토링 ID")
    @NotNull
    Long mentoringId,

    @Schema(description = "사전 질문")
    String preQuestion
) {
}
