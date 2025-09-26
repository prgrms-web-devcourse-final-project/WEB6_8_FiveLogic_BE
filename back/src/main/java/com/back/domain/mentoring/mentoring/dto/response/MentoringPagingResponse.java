package com.back.domain.mentoring.mentoring.dto.response;

import com.back.domain.mentoring.mentoring.dto.MentoringWithTagsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record MentoringPagingResponse(
    @Schema(description = "멘토링 목록")
    List<MentoringWithTagsDto> mentorings,
    @Schema(description = "현재 페이지 (0부터 시작)")
    int currentPage,
    @Schema(description = "총 페이지")
    int totalPage,
    @Schema(description = "총 개수")
    long totalElements,
    @Schema(description = "다음 페이지 존재 여부")
    boolean hasNext
) {
    public static MentoringPagingResponse from(Page<MentoringWithTagsDto> page) {
        return new MentoringPagingResponse(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext()
        );
    }
}
