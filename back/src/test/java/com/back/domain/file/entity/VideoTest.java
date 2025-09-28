package com.back.domain.file.entity;

import com.back.domain.file.video.entity.Video;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoTest {

    @Test
    @DisplayName("uuid, status, originalPath, originalFileName, duration, fileSize로 Video 객체 생성")
    void videoCreationTest() {
        String uuid = "sample-uuid";
        String status = "{\"status\":\"done\"}";
        String originalPath = "/videos/sample.mp4";
        Integer duration = 120;
        Long fileSize = 1024L;

        Video video = Video.create(uuid, status, originalPath, duration, fileSize);

        assertThat(video).isNotNull();
        assertThat(video.getUuid()).isEqualTo(uuid);
        assertThat(video.getStatus()).isEqualTo(status);
        assertThat(video.getPath()).isEqualTo(originalPath);
        assertThat(video.getDuration()).isEqualTo(duration);
        assertThat(video.getFileSize()).isEqualTo(fileSize);
    }

    @Test
    @DisplayName("uuid가 null 또는 공백일 경우 예외를 반환한다.")
    void videoCreationTestWithInvalidUuid() {
        String status = "{}";
        String originalPath = "/videos/sample.mp4";

        try {
            Video.create(null, status, originalPath, 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            Video.create("", status, originalPath, 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("originalPath가 null 또는 공백일 경우 예외를 반환한다.")
    void videoCreationTestWithInvalidOriginalPath() {
        String uuid = "sample-uuid";
        String status = "{}";

        try {
            Video.create(uuid, status, null, 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            Video.create(uuid, status, "", 100, 1000L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("트랜스코딩 상태 업데이트")
    void videoUpdateStatusTest() {
        String uuid = "sample-uuid";
        String status = "{\"status\":\"pending\"}";
        String originalPath = "/videos/sample.mp4";
        Integer duration = 120;
        Long fileSize = 1024L;

        Video video = Video.create(uuid, status, originalPath, duration, fileSize);
        assertThat(video.getStatus()).isEqualTo(status);

        String newStatus = "{\"status\":\"done\"}";
        video.updateStatus(newStatus);
        assertThat(video.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("트랜스코딩 상태 업데이트 시 null 또는 공백일 경우 예외를 반환한다.")
    void videoUpdateStatusTestWithInvalidStatus() {
        String uuid = "sample-uuid";
        String status = "{\"status\":\"pending\"}";
        String originalPath = "/videos/sample.mp4";
        Integer duration = 120;
        Long fileSize = 1024L;

        Video video = Video.create(uuid, status, originalPath, duration, fileSize);

        try {
            video.updateStatus(null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            video.updateStatus("");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
