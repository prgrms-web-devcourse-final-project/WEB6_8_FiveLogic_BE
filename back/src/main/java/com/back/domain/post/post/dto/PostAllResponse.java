package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

public record PostAllResponse(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt,
        int viewCount
) {
    public static PostAllResponse from(Post post) {
        return new PostAllResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthorName(),
                post.getCreateDate(),
                post.getViewCount()
        );
    }
}
