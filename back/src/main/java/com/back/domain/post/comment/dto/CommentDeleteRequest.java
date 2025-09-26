package com.back.domain.post.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDeleteRequest {
    @NotNull
    private Long CommentId;
}
