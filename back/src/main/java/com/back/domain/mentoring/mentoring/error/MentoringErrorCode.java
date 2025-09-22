package com.back.domain.mentoring.mentoring.error;

import com.back.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MentoringErrorCode implements ErrorCode {
    // 404
    NOT_FOUND_MENTOR("404-1", "멘토를 찾을 수 없습니다."),
    NOT_FOUND_MENTORING("404-2", "멘토링을 찾을 수 없습니다."),

    // 409
    ALREADY_EXISTS_MENTORING("409-1", "이미 멘토링 정보가 존재합니다.");

    private final String code;
    private final String message;
}
