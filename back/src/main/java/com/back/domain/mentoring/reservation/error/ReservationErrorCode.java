package com.back.domain.mentoring.reservation.error;

import com.back.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    // 404
    NOT_FOUND_RESERVATION("404-1", "예약이 존재하지 않습니다."),

    // 409
    NOT_AVAILABLE_SLOT("409-1", "이미 예약이 완료된 시간대입니다."),
    ALREADY_RESERVED_SLOT("409-2", "이미 예약한 시간대입니다. 예약 목록을 확인해 주세요."),
    CONCURRENT_RESERVATION_CONFLICT("409-3", "다른 사용자가 먼저 예약했습니다. 새로고침 후 다시 시도해 주세요.");

    private final String code;
    private final String message;
}
