package com.back.domain.news.news.dto;

import com.back.domain.news.news.entity.News;

import java.time.LocalDateTime;

public record NewsGetWithLikeResponse(
        Long id,
        String title,
        String videoUuid,
        String content,
        String authorId,
        boolean userLikeStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public NewsGetWithLikeResponse(News news, boolean userLikeStatus) {
        this(
                news.getId(),
                news.getTitle(),
                news.getVideo().getUuid(),
                news.getContent(),
                news.getMember().getName(),
                userLikeStatus,
                news.getCreateDate(),
                news.getModifyDate()
        );
    }
}
