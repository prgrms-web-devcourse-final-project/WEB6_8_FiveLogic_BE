package com.back.domain.post.dto;

import lombok.Data;

@Data
public class PostCreateRequest {
    private Long memberId;
    private String postType;
    private String title;
    private String content;

}
