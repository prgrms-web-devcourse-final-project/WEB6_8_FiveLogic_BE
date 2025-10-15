package com.back.domain.file.video.dto.controller;

import com.back.domain.file.video.dto.service.PresignedUrlResponse;

import java.time.LocalDateTime;

public record UploadUrlGetResponse(
        String url,
        String uuid,
        LocalDateTime expiresAt
) {
    public UploadUrlGetResponse(PresignedUrlResponse presignedUrlResponse) {
        this(
                presignedUrlResponse.url().toString(),
                extractUuid(presignedUrlResponse.url().toString()),
                presignedUrlResponse.expiresAt()
        );
    }

    private static String extractUuid(String url) {
        var matcher = java.util.regex.Pattern.compile(
                "([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})"
        ).matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }
}