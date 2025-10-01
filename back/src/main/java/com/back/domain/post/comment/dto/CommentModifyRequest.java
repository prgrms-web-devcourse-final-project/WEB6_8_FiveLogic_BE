package com.back.domain.post.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentModifyRequest(
        @NotNull
        Long commentId,

        @NotBlank(message = "공백일 수 없습니다.")
        String content
) {
}
