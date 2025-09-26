package com.back.domain.news.news.dto;

public record NewsCreateResponse(
        String title,
        String videoUrl,
        String content,
        String author
) {
    public NewsCreateResponse(String title, String videoUrl, String content, String author) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.content = content;
        this.author = author;
    }
}
