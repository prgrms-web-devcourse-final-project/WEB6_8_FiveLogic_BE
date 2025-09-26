package com.back.domain.file.dto.controller;

import java.net.URL;
import java.time.Instant;

public record UploadUrlGetResponse(String url, Instant expiresAt) {
    public UploadUrlGetResponse(URL url, Instant expiresAt) {
        this(url.toString(), expiresAt);
    }
}