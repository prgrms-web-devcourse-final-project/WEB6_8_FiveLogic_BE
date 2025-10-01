package com.back.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;


public record PostPagingResponse(
        @Schema(description = "게시글 목록")
        List<PostDto> posts,
        @Schema(description = "현재 페이지 (0부터 시작)")
        int currentPage,
        @Schema(description = "총 페이지")
        int totalPage,
        @Schema(description = "총 개수")
        long totalElements,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {


    public static PostPagingResponse from(Page<PostDto> page) {
        return new PostPagingResponse(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext()
        );
    }

}

