package com.back.domain.file.video.dto.service;

import java.net.URL;
import java.time.LocalDateTime;

public record PresignedUrlResponse(String url, LocalDateTime expiresAt) {
    public PresignedUrlResponse(URL url, LocalDateTime expiresAt) {
        this(url.toString(), expiresAt);
    }
}
