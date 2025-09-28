package com.back.domain.file.video.controller;

import com.back.domain.file.video.dto.controller.UploadUrlGetResponse;
import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import com.back.domain.file.video.service.FileManager;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class VideoController {
    private final FileManager fileManager;

    @GetMapping("/videos/upload")
    public RsData<UploadUrlGetResponse> getUploadUrl() {
        PresignedUrlResponse uploadUrl = fileManager.getUploadUrl();
        UploadUrlGetResponse response = new UploadUrlGetResponse(uploadUrl.url(), uploadUrl.expiresAt());
        return new RsData<>("200", "업로드용 URL 요청완료", response);
    }

    @GetMapping("/videos/download")
    public RsData<UploadUrlGetResponse> getDownloadUrls(@RequestParam String objectKey) {
        PresignedUrlResponse downloadUrl = fileManager.getDownloadUrl(objectKey);
        UploadUrlGetResponse response = new UploadUrlGetResponse(downloadUrl.url(), downloadUrl.expiresAt());
        return new RsData<>("200", "다운로드용 URL 요청완료", response);
    }
}