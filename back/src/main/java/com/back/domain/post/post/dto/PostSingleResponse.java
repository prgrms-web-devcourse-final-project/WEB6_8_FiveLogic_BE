package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;

import java.time.LocalDateTime;


public record PostSingleResponse(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt,
        int viewCount) {

    public static PostSingleResponse from(Post post) {
        return new PostSingleResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthorName(),
                post.getCreateDate(),
                post.getViewCount()
        );
    }
}
