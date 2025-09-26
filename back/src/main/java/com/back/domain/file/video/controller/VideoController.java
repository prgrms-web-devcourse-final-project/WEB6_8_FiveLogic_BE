package com.back.domain.file.video.controller;

import com.back.domain.file.video.dto.controller.UploadUrlGetResponse;
import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import com.back.domain.file.video.service.VideoService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
public class VideoController {

    private final VideoService videoService;

    // 업로드용 Presigned URL
    @GetMapping("/videos/upload-url")
    public RsData<UploadUrlGetResponse> getUploadUrl(@RequestParam String bucket, @RequestParam String fileName) {
        PresignedUrlResponse uploadUrl = videoService.generateUploadUrl(bucket, fileName);
        UploadUrlGetResponse response = new UploadUrlGetResponse(uploadUrl.url(), uploadUrl.expiresAt());
        return new RsData<>("201", "업로드용 URL이 생성되었습니다.", response);
    }

    // DASH 스트리밍용 URL
    @GetMapping("/videos/dash-urls")
    public Map<String, URL> getDashUrls(
            @RequestParam String bucket,
            @RequestParam String mpdFile,
            @RequestParam List<String> segmentFiles
    ) {
        return videoService.generateDashUrls(bucket, mpdFile, segmentFiles);
    }
}