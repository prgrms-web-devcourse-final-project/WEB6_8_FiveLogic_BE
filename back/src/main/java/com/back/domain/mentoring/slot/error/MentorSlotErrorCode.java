package com.back.domain.mentoring.slot.error;

import com.back.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MentorSlotErrorCode implements ErrorCode {

    // 400 DateTime 체크
    START_TIME_REQUIRED("400-1", "시작 일시와 종료 일시는 필수입니다."),
    END_TIME_REQUIRED("400-2", "시작 일시와 종료 일시는 필수입니다."),
    START_TIME_IN_PAST("400-3", "시작 일시는 현재 이후여야 합니다."),
    END_TIME_BEFORE_START("400-4", "종료 일시는 시작 일시보다 이후여야 합니다."),
    INSUFFICIENT_SLOT_DURATION("400-5", "슬롯은 최소 30분 이상이어야 합니다."),

    // 400 Slot 체크
    CANNOT_UPDATE_RESERVED_SLOT("400-6", "예약된 슬롯은 수정할 수 없습니다."),
    CANNOT_DELETE_RESERVED_SLOT("400-7", "예약된 슬롯은 삭제할 수 없습니다."),

    // 403
    NOT_OWNER("403-1", "접근 권한이 없습니다."),

    // 404
    NOT_FOUND_MENTOR_SLOT("404-1", "일정 정보가 없습니다."),

    // 409
    OVERLAPPING_SLOT("409-1", "선택한 시간은 이미 예약된 시간대입니다.");

    private final String code;
    private final String message;
}
