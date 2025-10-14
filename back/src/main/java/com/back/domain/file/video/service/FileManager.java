package com.back.domain.file.video.service;


import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileManager {
    private final VideoService videoService;
    private final S3Service s3Service;

    public PresignedUrlResponse getUploadUrl(String filename) {
        String contentType = validateAndGetContentType(filename);
        String ext = extractExt(filename);
        String uuid = UUID.randomUUID().toString();
        String objectKey = "videos/" + uuid + "." + ext;
        Integer expires = 5;
        URL url = s3Service.generateUploadUrl(objectKey, expires, contentType);
        videoService.createVideo(uuid, "{\"status\":\"READY_TO_UPLOAD\"}", objectKey, 0);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expires);
        return new PresignedUrlResponse(url, expiresAt);
    }

    private String extractExt(String filename) {
        int pos = filename.lastIndexOf(".");
        return filename.substring(pos + 1);
    }

    private String validateAndGetContentType(String filename) {
        String ext = extractExt(filename).toLowerCase();
        switch (ext) {
            case "mp4":
                return "video/mp4";
            case "mov":
                return "video/quicktime";
            case "avi":
                return "video/x-msvideo";
            case "wmv":
                return "video/x-ms-wmv";
            case "mkv":
                return "video/x-matroska";
            case "webm":
                return "video/webm";
            default:
                throw new ServiceException("400", "지원하지 않는 동영상 파일 형식입니다: " + ext);
        }
    }

    public PresignedUrlResponse getDownloadUrl(String objectKey) {
        Integer expires = 60;
        URL url = s3Service.generateDownloadUrl(objectKey, expires);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expires);
        return new PresignedUrlResponse(url, expiresAt);
    }

    //TODO : 테스트 작성필요
    public void updateVideoStatus(String videoId, String status) {
        try {
            videoService.updateStatus(videoId, status);
        } catch (Exception e) {
            videoService.createVideo(videoId, status, "/", 0);
        }
    }
}
