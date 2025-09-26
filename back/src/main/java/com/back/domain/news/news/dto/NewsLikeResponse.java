package com.back.domain.news.news.dto;


public record NewsLikeResponse(
        Long memberId,
        Long newsId,
        Long like
) {
}