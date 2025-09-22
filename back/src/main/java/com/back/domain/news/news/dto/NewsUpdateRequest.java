package com.back.domain.news.news.dto;

public record NewsUpdateRequest(
        Long newsId,
        String title,
        String content,
        String videoUuid
) {}
