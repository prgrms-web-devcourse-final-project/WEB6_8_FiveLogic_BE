package com.back.domain.file.video.dto.service;

import java.net.URL;
import java.time.Instant;

public record PresignedUrlResponse(String url, Instant expiresAt) {
    public PresignedUrlResponse(URL url, Instant expiresAt) {
        this(url.toString(), expiresAt);
    }
}
