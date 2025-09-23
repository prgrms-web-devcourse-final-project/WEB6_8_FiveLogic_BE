package com.back.domain.mentoring.mentoring.error;

import com.back.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MentoringErrorCode implements ErrorCode {

    // 400
    CANNOT_DELETE_MENTORING("400-1", "예약 이력이 있는 멘토링은 삭제할 수 없습니다."),

    // 403
    FORBIDDEN_NOT_OWNER("403-1", "해당 멘토링에 대한 권한이 없습니다."),

    // 404
    NOT_FOUND_MENTOR("404-1", "멘토를 찾을 수 없습니다."),
    NOT_FOUND_MENTORING("404-2", "멘토링을 찾을 수 없습니다."),

    // 409
    ALREADY_EXISTS_MENTORING("409-1", "이미 멘토링 정보가 존재합니다.");

    private final String code;
    private final String message;
}
