package com.back.domain.news.news.dto;

import com.back.domain.news.news.entity.News;

public record NewsUpdateResponse(
        Long id,
        String title,
        String content,
        String videoUuid,
        String authorName
) {
    public NewsUpdateResponse(News news) {
        this(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.getVideo().getUuid(),
                news.getMember().getName()
        );
    }
}
