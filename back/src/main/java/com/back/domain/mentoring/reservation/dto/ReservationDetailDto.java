package com.back.domain.mentoring.reservation.dto;

import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReservationDetailDto(
    @Schema(description = "예약 ID")
    Long reservationId,
    @Schema(description = "예약 상태")
    ReservationStatus status,
    @Schema(description = "사전 질문")
    String preQuestion,

    @Schema(description = "멘토 슬롯 ID")
    Long mentorSlotId,
    @Schema(description = "시작 일시")
    LocalDateTime startDateTime,
    @Schema(description = "종료 일시")
    LocalDateTime endDateTime,

    @Schema(description = "생성일")
    LocalDateTime createDate,
    @Schema(description = "수정일")
    LocalDateTime modifyDate
) {
    public static ReservationDetailDto from(Reservation reservation) {
        return new ReservationDetailDto(
            reservation.getId(),
            reservation.getStatus(),
            reservation.getPreQuestion(),
            reservation.getMentorSlot().getId(),
            reservation.getMentorSlot().getStartDateTime(),
            reservation.getMentorSlot().getEndDateTime(),
            reservation.getCreateDate(),
            reservation.getModifyDate()
        );
    }
}
