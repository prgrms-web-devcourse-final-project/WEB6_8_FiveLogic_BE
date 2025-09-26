package com.back.domain.post.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Data
public class PostPagingResponse{

    @Schema(description = "게시글 목록")
    List<PostDto> posts;
    @Schema(description = "현재 페이지 (0부터 시작)")
    int currentPage;
    @Schema(description = "총 페이지")
    int totalPage;
    @Schema(description = "총 개수")
    long totalElements;
    @Schema(description = "다음 페이지 존재 여부")
    boolean hasNext;

    public static PostPagingResponse from(Page<PostDto> page) {
        PostPagingResponse postPagingResponse = new PostPagingResponse();
        postPagingResponse.setPosts(page.getContent());
        postPagingResponse.setCurrentPage(page.getNumber());
        postPagingResponse.setTotalPage(page.getTotalPages());
        postPagingResponse.setTotalElements(page.getTotalElements());
        postPagingResponse.setHasNext(page.hasNext());

        return postPagingResponse;
    }

}

