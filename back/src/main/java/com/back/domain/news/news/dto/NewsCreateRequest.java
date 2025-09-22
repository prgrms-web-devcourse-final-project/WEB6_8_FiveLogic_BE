package com.back.domain.news.news.dto;

public record NewsCreateRequest(
        String title,
        String videoUuid,
        String content
        ) {
}
