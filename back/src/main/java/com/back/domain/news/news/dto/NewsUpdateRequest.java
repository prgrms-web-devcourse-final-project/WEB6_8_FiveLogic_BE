package com.back.domain.news.news.dto;

public record NewsUpdateRequest(
        String title,
        String content,
        String videoUuid
) {}
