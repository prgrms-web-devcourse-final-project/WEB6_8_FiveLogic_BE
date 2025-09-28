package com.back.domain.file.video.dto.controller;

import java.net.URL;
import java.time.LocalDateTime;

public record UploadUrlGetResponse(
        URL url,
        LocalDateTime expiresAt
) {
}