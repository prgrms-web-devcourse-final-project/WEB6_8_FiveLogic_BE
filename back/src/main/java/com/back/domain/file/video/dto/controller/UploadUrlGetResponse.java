package com.back.domain.file.video.dto.controller;

import java.net.URL;
 import java.time.LocalDateTime;

public record UploadUrlGetResponse(String url, LocalDateTime expiresAt) {
    public UploadUrlGetResponse(URL url, LocalDateTime expiresAt) {
        this(url.toString(), expiresAt);
    }
}