package com.back.domain.mentoring.reservation.error;

import com.back.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    NOT_AVAILABLE_SLOT("400-1", "예약할 수 없는 슬롯입니다."),
    ALREADY_RESERVED_SLOT("400-2", "해당 시간에 예약 내역이 있습니다. 예약 목록을 확인해 주세요."),

    NOT_FOUND_RESERVATION("404-1", "예약이 존재하지 않습니다.");

    private final String code;
    private final String message;
}
