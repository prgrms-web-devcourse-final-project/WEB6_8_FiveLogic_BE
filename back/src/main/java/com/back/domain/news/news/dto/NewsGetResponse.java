package com.back.domain.news.news.dto;

import com.back.domain.news.news.entity.News;

import java.time.LocalDateTime;

public record NewsGetResponse(
        String title,
        String videoUuid,
        String content,
        String authorId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public NewsGetResponse(News news) {
        this(
                news.getTitle(),
                news.getVideo().getUuid(),
                news.getContent(),
                news.getMember().getName(),
                news.getCreateDate(),
                news.getModifyDate()
        );
    }
}
