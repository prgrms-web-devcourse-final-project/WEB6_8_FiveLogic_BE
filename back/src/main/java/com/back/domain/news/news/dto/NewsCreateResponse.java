package com.back.domain.news.news.dto;

public record NewsCreateResponse(
        Long newsId,
        String title,
        String videoUuid,
        String content,
        String author
) {}
