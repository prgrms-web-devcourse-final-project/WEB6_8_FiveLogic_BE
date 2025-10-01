package com.back.domain.post.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        String role,

        @NotBlank(message = "댓글을 입력해주세요")
        String comment
) {

}
