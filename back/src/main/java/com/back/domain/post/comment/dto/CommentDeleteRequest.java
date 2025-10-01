package com.back.domain.post.comment.dto;

import jakarta.validation.constraints.NotNull;

public record CommentDeleteRequest(@NotNull
                                   Long commentId
) {

}
