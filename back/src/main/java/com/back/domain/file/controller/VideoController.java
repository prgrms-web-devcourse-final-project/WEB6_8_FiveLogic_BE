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

    /**
     * fileName을 입력받아 버킷에 업로드할 수 있는 URL을 반환합니다.
     */
    @GetMapping("/videos/upload-url")
    public URL getUploadUrl(@RequestParam String fileName) {
        return videoService.generateUploadUrl("test-bucket", fileName);
    }

    /**
     * Dash 스트리밍용 URL들을 반환합니다. .mdf파일과 세그먼트 파일의 리스트를 반환합니다.
     */
    // DASH 스트리밍용 URL
    @GetMapping("/videos/dash-urls")
    public Map<String, URL> getDashUrls(
            @RequestParam String mpdFile,
            @RequestParam List<String> segmentFiles
    ) {
        return videoService.generateDashUrls("test-bucket", mpdFile, segmentFiles);
    }
}