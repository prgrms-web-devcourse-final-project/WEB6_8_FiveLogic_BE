package com.back.domain.file.video.dto;

import java.time.LocalDateTime;

public record DownLoadUrlGetResponse(
        String url,
        LocalDateTime expiresAt
) {
}