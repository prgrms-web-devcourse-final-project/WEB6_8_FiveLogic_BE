package com.back.domain.file.entity;

import com.back.domain.file.video.entity.Video;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoTest {

    @Test
    @DisplayName("uuid, transcodingResults, originalPath, originalFileName, duration, fileSize로 Video 객체 생성")
    void videoCreationTest() {
        String uuid = "sample-uuid";
        String transcodingResults = "{\"status\":\"done\"}";
        String originalPath = "/videos/sample.mp4";
        Integer duration = 120;
        Long fileSize = 1024L;

        Video video = Video.create(uuid, transcodingResults, originalPath, duration, fileSize);

        assertThat(video).isNotNull();
        assertThat(video.getUuid()).isEqualTo(uuid);
        assertThat(video.getStatus()).isEqualTo(transcodingResults);
        assertThat(video.getPath()).isEqualTo(originalPath);
        assertThat(video.getDuration()).isEqualTo(duration);
        assertThat(video.getFileSize()).isEqualTo(fileSize);
    }

    @Test
    @DisplayName("uuid가 null 또는 공백일 경우 예외를 반환한다.")
    void videoCreationTestWithInvalidUuid() {
        String transcodingResults = "{}";
        String originalPath = "/videos/sample.mp4";

        try {
            Video.create(null, transcodingResults, originalPath, 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            Video.create("", transcodingResults, originalPath, 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("originalPath가 null 또는 공백일 경우 예외를 반환한다.")
    void videoCreationTestWithInvalidOriginalPath() {
        String uuid = "sample-uuid";
        String transcodingResults = "{}";

        try {
            Video.create(uuid, transcodingResults, null, 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            Video.create(uuid, transcodingResults, "", 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
