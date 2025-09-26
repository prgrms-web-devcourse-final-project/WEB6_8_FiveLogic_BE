package com.back.domain.post.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentModifyRequest {
    @NotNull
    private Long commentId;
    @NotBlank(message = "공백일 수 없습니다.")
    private String content;
}
