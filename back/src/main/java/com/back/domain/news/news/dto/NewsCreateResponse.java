package com.back.domain.news.news.dto;

public record NewsCreateResponse(
        String title,
        String videoUrl,
        String content,
        String author
) {}
