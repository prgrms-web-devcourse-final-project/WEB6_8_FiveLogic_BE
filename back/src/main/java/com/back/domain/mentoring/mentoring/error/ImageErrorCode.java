package com.back.domain.mentoring.mentoring.error;

import com.back.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    // 400
    FILE_SIZE_EXCEEDED("400-1", "이미지 파일 크기는 10MB를 초과할 수 없습니다."),
    INVALID_FILE_TYPE("400-2", "이미지 파일만 업로드 가능합니다."),
    UNSUPPORTED_IMAGE_FORMAT("400-3", "JPG, PNG 형식만 업로드 가능합니다."),
    IMAGE_UPLOAD_FAILED("400-4", "이미지 업로드에 실패했습니다.");

    private final String code;
    private final String message;
}
