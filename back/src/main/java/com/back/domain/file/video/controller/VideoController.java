package com.back.domain.file.video.controller;

import com.back.domain.file.video.dto.controller.UploadUrlGetResponse;
import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import com.back.domain.file.video.service.FileManager;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VideoController {
    private final FileManager fileManager;

    @GetMapping("/videos/upload")
    @Operation(summary="업로드용 URL 요청", description="파일 업로드를 위한 Presigned URL을 발급받습니다.")
    public RsData<UploadUrlGetResponse> getUploadUrl() {
        PresignedUrlResponse uploadUrl = fileManager.getUploadUrl();
        UploadUrlGetResponse response = new UploadUrlGetResponse(uploadUrl.url().toString(), uploadUrl.expiresAt());
        return new RsData<>("200", "업로드용 URL 요청완료", response);
    }

    @GetMapping("/videos/download")
    @Operation(summary="다운로드용 URL 요청", description="파일 다운로드를 위한 Presigned URL을 발급받습니다.")
    public RsData<UploadUrlGetResponse> getDownloadUrls(@RequestParam String objectKey) {
        PresignedUrlResponse downloadUrl = fileManager.getDownloadUrl(objectKey);
        UploadUrlGetResponse response = new UploadUrlGetResponse(downloadUrl.url().toString(), downloadUrl.expiresAt());
        return new RsData<>("200", "다운로드용 URL 요청완료", response);
    }
}