package com.back.fixture;

import com.back.domain.file.video.entity.Video;

import java.util.UUID;

public class VideoFixture {
    private String uuid = UUID.randomUUID().toString();
    private String transcodingResults = """
                {
                  "1080p": {
                    "file_path": "/path/to/1080p/video.mpd",
                    "status": "COMPLETED",
                    "size_mb": 150
                  },
                  "720p": {
                    "file_path": "/path/to/720p/video.mpd",
                    "status": "IN_PROGRESS",
                    "size_mb": 80
                  },
                  "480p": {
                    "file_path": "/path/to/480p/video.mpd",
                    "status": "FAILED",
                    "size_mb": 40
                  }
                }
                """;
    private String originalPath = "/videos/original.mp4";
    private String originalFileName = "original.mp4";
    private Integer duration = 120;
    private Long fileSize = 1024L * 1024L * 10; // 10MB

    private static VideoFixture builder() {
        return new VideoFixture();
    }

    public static Video createDefault() {
        return builder().build();
    }
    public static Video create(String uuid, String transcodingResults, String originalPath, String originalFileName, Integer duration, Long fileSize) {
        return builder()
                .withUuid(uuid)
                .withTranscodingResults(transcodingResults)
                .withOriginalPath(originalPath)
                .withOriginalFileName(originalFileName)
                .withDuration(duration)
                .withFileSize(fileSize)
                .build();
    }

    public VideoFixture withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public VideoFixture withTranscodingResults(String transcodingResults) {
        this.transcodingResults = transcodingResults;
        return this;
    }

    public VideoFixture withOriginalPath(String originalPath) {
        this.originalPath = originalPath;
        return this;
    }

    public VideoFixture withOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
        return this;
    }

    public VideoFixture withDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public VideoFixture withFileSize(Long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public Video build() {
        return Video.create(
                uuid,
                transcodingResults,
                originalPath,
                originalFileName,
                duration,
                fileSize
        );
    }
}