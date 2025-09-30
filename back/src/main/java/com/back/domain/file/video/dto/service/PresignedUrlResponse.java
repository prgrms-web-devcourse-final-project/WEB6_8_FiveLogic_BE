package com.back.domain.file.video.dto.service;

import java.net.URL;
import java.time.LocalDateTime;

public record PresignedUrlResponse(
        URL url,
        LocalDateTime expiresAt
) {
}
