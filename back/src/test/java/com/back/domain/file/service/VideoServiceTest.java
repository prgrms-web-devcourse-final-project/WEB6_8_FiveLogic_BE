package com.back.domain.file.service;

import com.back.domain.file.video.entity.Video;
import com.back.domain.file.video.repository.VideoRepository;
import com.back.domain.file.video.service.VideoService;
import com.back.fixture.VideoFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    @Mock
    private VideoRepository videoRepository;
    @InjectMocks
    private VideoService videoService;

    @Test
    @DisplayName("transcodingResults, originalPath, originalFileName, duration로 Video 객체 생성")
    void videoCreationTest() {
        String uuid = UUID.randomUUID().toString();
        String transcodingResults = """
                {
                  "1080p": {
                    "file_path": "/path/to/1080p/video.mpd",
                    "status": "COMPLETED",
                    "size_mb": 150
                  },
                  "720p": {
                    "file_path": "/path/to/720p/video.mpd",
                    "status": "COMPLETED",
                    "size_mb": 80
                  },
                  "480p": {
                    "file_path": "/path/to/480p/video.mpd",
                    "status": "COMPLETED",
                    "size_mb": 40
                  }
                }
                """;
        String originalPath = "/videos/sample.mp4";
        Integer duration = 120;

        Video video = VideoFixture.create(uuid, transcodingResults, originalPath, duration);
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        Video createdVideo = videoService.createVideo(uuid, transcodingResults, originalPath, duration);
        assertThat(createdVideo).isNotNull();
        assertThat(createdVideo.getUuid()).isEqualTo(uuid);
        assertThat(createdVideo.getStatus()).isEqualTo(transcodingResults);
        assertThat(createdVideo.getPath()).isEqualTo(originalPath);
        assertThat(createdVideo.getDuration()).isEqualTo(duration);
    }

    @Test
    @DisplayName("uuid로 Video 객체 조회")
    void getNewsByUuidTest() {
        String uuid = UUID.randomUUID().toString();
        Video video = VideoFixture.createDefault();
        when(videoRepository.findByUuid(uuid)).thenReturn(java.util.Optional.of(video));

        Video foundVideo = videoService.getNewsByUuid(uuid);
        assertThat(foundVideo).isNotNull();
        assertThat(foundVideo.getUuid()).isEqualTo(video.getUuid());
    }

    @Test
    @DisplayName("존재하지 않는 uuid로 Video 객체 조회 시 예외 발생")
    void getNewsByUuidNotFoundTest() {
        String uuid = UUID.randomUUID().toString();
        when(videoRepository.findByUuid(uuid)).thenReturn(java.util.Optional.empty());

        try {
            videoService.getNewsByUuid(uuid);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceException.class);
            assertThat(e.getMessage()).isEqualTo("404 : Video not found");
        }
    }

    @Test
    @DisplayName("Video 객체 상태 업데이트")
    void updateStatusTest() {
        String uuid = UUID.randomUUID().toString();
        String newStatus = "{\"status\":\"pending\"}";
        Video video = VideoFixture.createDefault();
        when(videoRepository.findByUuid(uuid)).thenReturn(java.util.Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        Video updatedVideo = videoService.updateStatus(uuid, newStatus);
        assertThat(updatedVideo).isNotNull();
        assertThat(updatedVideo.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("Video 객체 상태 업데이트 시 Null 혹은 공백일 때 예외 발생")
    void updateStatusInvalidTest() {
        String uuid = UUID.randomUUID().toString();

        try {
            videoService.updateStatus(uuid, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceException.class);
            assertThat(e.getMessage()).isEqualTo("400 : status cannot be null or empty");
        }

        try {
            videoService.updateStatus(uuid, "   ");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceException.class);
            assertThat(e.getMessage()).isEqualTo("400 : status cannot be null or empty");
        }
    }
}