package com.back.domain.file.video.controller;

import com.back.domain.file.video.dto.DownLoadUrlGetResponse;
import com.back.domain.file.video.dto.controller.UploadUrlGetResponse;
import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import com.back.domain.file.video.service.FileManager;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VideoController {
    private final FileManager fileManager;

    @GetMapping("/videos/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "업로드용 URL 요청", description = "파일 업로드를 위한 Presigned URL을 발급받습니다.")
    public RsData<UploadUrlGetResponse> getUploadUrl(@RequestParam String filename) {
        PresignedUrlResponse uploadUrl = fileManager.getUploadUrl(filename);
        UploadUrlGetResponse response = new UploadUrlGetResponse(uploadUrl);
        return new RsData<>("200", "업로드용 URL 요청완료", response);
    }

    @GetMapping("/videos/download")
    @Operation(summary = "다운로드용 URL 요청", description = "파일 다운로드를 위한 Presigned URL을 발급받습니다.")
    public RsData<DownLoadUrlGetResponse> getDownloadUrls(@RequestParam String uuid, @RequestParam String resolution) {
        PresignedUrlResponse downloadUrl = fileManager.getDownloadUrl(uuid, resolution);
        DownLoadUrlGetResponse response = new DownLoadUrlGetResponse(downloadUrl.url().toString(), downloadUrl.expiresAt());
        return new RsData<>("200", "다운로드용 URL 요청완료", response);
    }
}