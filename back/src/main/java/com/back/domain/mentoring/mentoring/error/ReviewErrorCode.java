package com.back.domain.mentoring.mentoring.error;

import com.back.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    // 400
    CANNOT_REVIEW("400-1", "멘토링 진행 전입니다. 리뷰를 작성할 수 없습니다."),
    INVALID_RATING_UNIT("400-2", "평점은 0.5 단위로 입력해야 합니다."),

    // 403
    FORBIDDEN_NOT_MENTEE("403-1", "예약의 멘티만 리뷰를 작성할 수 있습니다."),

    // 404
    REVIEW_NOT_FOUND("404-1", "리뷰를 찾을 수 없습니다."),

    // 409
    ALREADY_EXISTS_REVIEW("409-1", "이미 리뷰가 존재합니다.");

    private final String code;
    private final String message;
}
