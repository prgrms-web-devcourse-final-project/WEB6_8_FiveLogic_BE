package com.back.domain.post.comment.dto;

import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long postId;
    private String role;
    private String comment;
}
