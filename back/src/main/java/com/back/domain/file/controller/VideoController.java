package com.back.domain.file.controller;

import com.back.domain.file.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // 업로드용 Presigned URL
    @GetMapping("/videos/upload-url")
    public URL getUploadUrl(@RequestParam String bucket, @RequestParam String fileName) {
        return videoService.generateUploadUrl(bucket, fileName);
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