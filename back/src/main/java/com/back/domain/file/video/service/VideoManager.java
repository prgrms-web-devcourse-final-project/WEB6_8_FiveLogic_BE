package com.back.domain.file.video.service;


import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoManager {
    private final VideoService videoService;
    private final S3Service s3Service;

    public PresignedUrlResponse getUploadUrl() {
        String uuid = UUID.randomUUID().toString();
        Integer expires = 5;
        URL url = s3Service.generateUploadUrl("videos", uuid, expires);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expires);
        return new PresignedUrlResponse(url, expiresAt);
    }

    public PresignedUrlResponse getDownloadUrl(String objectKey) {
        Integer expires = 60;
        URL url = s3Service.generateDownloadUrl("videos", objectKey, expires);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expires);
        return new PresignedUrlResponse(url, expiresAt);
    }
}
