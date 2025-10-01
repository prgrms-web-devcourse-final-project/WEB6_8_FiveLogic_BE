package com.back.domain.roadmap.roadmap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "직업 로드맵 페이징 조회 응답")
public record JobRoadmapPagingResponse(
        @Schema(description = "직업 로드맵 목록")
        List<JobRoadmapListResponse> jobRoadmaps,

        @Schema(description = "현재 페이지 (0부터 시작)")
        int currentPage,

        @Schema(description = "총 페이지")
        int totalPage,

        @Schema(description = "총 개수")
        long totalElements,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static JobRoadmapPagingResponse from(Page<JobRoadmapListResponse> page) {
        return new JobRoadmapPagingResponse(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}