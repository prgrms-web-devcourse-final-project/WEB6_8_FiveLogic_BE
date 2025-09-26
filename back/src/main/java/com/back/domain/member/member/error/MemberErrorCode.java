package com.back.domain.member.member.error;

import com.back.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    NOT_FOUND_MEMBER("404-1", "회원을 찾을 수 없습니다."),
    NOT_FOUND_MENTOR("404-2", "멘토를 찾을 수 없습니다."),
    NOT_FOUND_MENTEE("404-3", "멘티를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
