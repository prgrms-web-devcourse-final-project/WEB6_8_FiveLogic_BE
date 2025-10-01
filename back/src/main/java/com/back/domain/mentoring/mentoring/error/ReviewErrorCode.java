package com.back.domain.mentoring.mentoring.error;

import com.back.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    FORBIDDEN_NOT_MENTEE("403-1", "예약의 멘티만 리뷰를 작성할 수 있습니다.");

    private final String code;
    private final String message;
}
