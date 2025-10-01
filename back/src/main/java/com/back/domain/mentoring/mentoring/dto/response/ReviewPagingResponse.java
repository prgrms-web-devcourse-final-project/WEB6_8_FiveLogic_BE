package com.back.domain.mentoring.mentoring.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record ReviewPagingResponse(
    List<ReviewResponse> reviews,
    @Schema(description = "현재 페이지 (0부터 시작)")
    int currentPage,
    @Schema(description = "총 페이지")
    int totalPage,
    @Schema(description = "총 개수")
    long totalElements,
    @Schema(description = "다음 페이지 존재 여부")
    boolean hasNext
) {
    public static ReviewPagingResponse from(Page<ReviewResponse> page) {
        return new ReviewPagingResponse(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext()
        );
    }
}
