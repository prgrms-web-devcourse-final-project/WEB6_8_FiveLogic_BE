package com.back.domain.mentoring.reservation.error;

import com.back.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    // 400
    CANNOT_APPROVE("400-1", "예약 요청 상태가 아닙니다. 수락이 불가능합니다."),
    CANNOT_REJECT("400-2", "예약 요청 상태가 아닙니다. 거절이 불가능합니다."),
    CANNOT_CANCEL("400-3", "예약 요청 상태 또는 예약 승인 상태가 아닙니다. 취소가 불가능합니다."),
    CANNOT_COMPLETE("400-4", "예약 승인 상태가 아닙니다. 완료가 불가능합니다."),
    INVALID_MENTOR_SLOT("400-5", "이미 시간이 지난 슬롯입니다. 예약 상태 변경이 불가능합니다."),
    MENTORING_NOT_STARTED("400-6", "멘토링이 시작되지 않았습니다. 완료가 불가능합니다."),


    // 403
    FORBIDDEN_NOT_MENTOR("403-1", "해당 예약에 대한 멘토 권한이 없습니다."),
    FORBIDDEN_NOT_MENTEE("403-2", "해당 예약에 대한 멘티 권한이 없습니다."),

    // 404
    NOT_FOUND_RESERVATION("404-1", "예약이 존재하지 않습니다."),

    // 409
    NOT_AVAILABLE_SLOT("409-1", "이미 예약이 완료된 시간대입니다."),
    ALREADY_RESERVED_SLOT("409-2", "이미 예약한 시간대입니다. 예약 목록을 확인해 주세요."),
    CONCURRENT_RESERVATION_CONFLICT("409-3", "다른 사용자가 먼저 예약했습니다. 새로고침 후 다시 시도해 주세요."),
    CONCURRENT_APPROVAL_CONFLICT("409-4", "이미 수락한 예약입니다.");

    private final String code;
    private final String message;
}
