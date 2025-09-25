package com.back.domain.post.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long postId;
    private String role;
    @NotBlank(message = "댓글을 입력해주세요")
    private String comment;
}
