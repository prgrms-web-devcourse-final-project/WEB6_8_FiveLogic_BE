package com.back.domain.mentoring.reservation.dto;

import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.entity.MentoringSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReservationDto(
    @Schema(description = "예약 ID")
    Long reservationId,
    @Schema(description = "예약 상태")
    ReservationStatus status,

    @Schema(description = "멘토링 ID")
    Long mentoringId,
    @Schema(description = "멘토링 제목")
    String title,

    @Schema(description = "멘토 슬롯 ID")
    Long mentorSlotId,
    @Schema(description = "시작 일시")
    LocalDateTime startDateTime,
    @Schema(description = "종료 일시")
    LocalDateTime endDateTime,
    @Schema(description = "멘토링 세션 ID")
    Long mentoringSessionId
) {
    public static ReservationDto from(Reservation reservation, MentoringSession mentoringSession) {
        return new ReservationDto(
            reservation.getId(),
            reservation.getStatus(),
            reservation.getMentoring().getId(),
            reservation.getMentoring().getTitle(),
            reservation.getMentorSlot().getId(),
            reservation.getMentorSlot().getStartDateTime(),
            reservation.getMentorSlot().getEndDateTime(),
            mentoringSession != null ? mentoringSession.getId() : null
        );
    }
}
