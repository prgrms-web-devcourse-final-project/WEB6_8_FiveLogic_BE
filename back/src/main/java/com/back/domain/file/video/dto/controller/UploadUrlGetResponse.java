package com.back.domain.file.video.dto.controller;

import java.time.LocalDateTime;

public record UploadUrlGetResponse(
        String url,
        String uuid,
        LocalDateTime expiresAt
) {
}