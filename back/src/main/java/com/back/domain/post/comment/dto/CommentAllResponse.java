package com.back.domain.post.comment.dto;

import com.back.domain.post.comment.entity.PostComment;
import lombok.Data;

import java.time.LocalDateTime;


public record CommentAllResponse(
        Long id,
        String content,
        String authorName,
        LocalDateTime createdAt,
        Long memberId
) {


    public static CommentAllResponse from(PostComment comment) {
        return new CommentAllResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthorName(),
                comment.getCreateDate(),
                comment.getAuthorId()
        );
    }
}
