package com.back.domain.file.service;

import com.back.domain.file.entity.Video;
import com.back.domain.file.repository.VideoRepository;
import com.back.fixture.VideoFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private S3Presigner presigner;
    @Mock
    private S3Client s3Client;
    @InjectMocks
    private VideoService videoService;

    @Test
    @DisplayName("transcodingResults, originalPath, originalFileName, duration, fileSize로 Video 객체 생성")
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
        String originalFileName = "sample.mp4";
        Integer duration = 120;
        Long fileSize = 1024L;

        Video video = VideoFixture.create(uuid, transcodingResults, originalPath, originalFileName, duration, fileSize);
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        Video createdVideo = videoService.createVideo(transcodingResults, originalPath, originalFileName, duration, fileSize);
        assertThat(createdVideo).isNotNull();
        assertThat(createdVideo.getUuid()).isEqualTo(uuid);
        assertThat(createdVideo.getTranscodingResults()).isEqualTo(transcodingResults);
        assertThat(createdVideo.getOriginalPath()).isEqualTo(originalPath);
        assertThat(createdVideo.getViews()).isEqualTo(0);
        assertThat(createdVideo.getOriginalFileName()).isEqualTo(originalFileName);
        assertThat(createdVideo.getDuration()).isEqualTo(duration);
        assertThat(createdVideo.getFileSize()).isEqualTo(fileSize);
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
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("존재하지 않는 비디오입니다.");
        }
    }

    @Test
    @DisplayName("S3 업로드 URL 생성")
    void generateUploadUrlTest() throws MalformedURLException {
        String bucket = "test-bucket";
        String objectKey = "test-video.mp4";

        PresignedPutObjectRequest mocked = mock(PresignedPutObjectRequest.class);

        when(presigner.presignPutObject(any(Consumer.class))).thenReturn(mocked);

        when(mocked.url()).thenReturn(new URL("http://localhost:8080/upload"));

        URL url = videoService.generateUploadUrl(bucket, objectKey);

        assertThat(url).isNotNull();
        assertThat(url.toString()).isEqualTo("http://localhost:8080/upload");
    }

    @Test
    @DisplayName("S3 다운로드 URL 생성")
    void generateDownloadUrlTest() throws MalformedURLException {
        String bucket = "test-bucket";
        String objectKey = "test-video.mp4";

        var mocked = mock(software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest.class);

        when(presigner.presignGetObject(any(Consumer.class))).thenReturn(mocked);

        when(mocked.url()).thenReturn(new URL("http://localhost:8080/download"));

        URL url = videoService.generateDownloadUrl(bucket, objectKey);

        assertThat(url).isNotNull();
        assertThat(url.toString()).isEqualTo("http://localhost:8080/download");
    }

    @Test
    @DisplayName("업로드 URL 요청의 결과가 null일 경우 예외 발생")
    void generateUploadUrl_PresignRequestNull_Test() {
        String bucket = "test-bucket";
        String objectKey = "test-video.mp4";

        when(presigner.presignPutObject(any(Consumer.class))).thenReturn(null);

        try {
            videoService.generateUploadUrl(bucket, objectKey);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    @DisplayName("다운로드 URL 요청의 결과가 null일 경우 예외 발생")
    void generateDownloadUrl_PresignRequestNull_Test() {
        String bucket = "test-bucket";
        String objectKey = "test-video.mp4";

        when(presigner.presignGetObject(any(Consumer.class))).thenReturn(null);

        try {
            videoService.generateDownloadUrl(bucket, objectKey);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }


    @Test
    @DisplayName("isExist() - 객체 존재 시 true 반환")
    void isExist_objectExists_shouldReturnTrue() {
        String bucket = "test-bucket";
        String objectKey = "existing.mp4";

        HeadObjectResponse mockResponse = mock(HeadObjectResponse.class);
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        boolean exist = videoService.isExist(bucket, objectKey);

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("isExist() - 객체 존재하지 않으면 false 반환")
    void isExist_objectNotExists_shouldReturnFalse() {
        String bucket = "test-bucket";
        String objectKey = "non-existing.mp4";

        doThrow(S3Exception.builder().statusCode(404).build())
                .when(s3Client).headObject(any(HeadObjectRequest.class));

        boolean exist = videoService.isExist(bucket, objectKey);

        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("generateDashUrls() - MPD와 segment URL 정상 생성")
    void generateDashUrls_shouldReturnUrls() throws Exception {
        String bucket = "test-bucket";
        String mpdFile = "video.mpd";
        List<String> segmentFiles = List.of("seg1.mp4", "seg2.mp4");

        PresignedGetObjectRequest mockedPresigned = mock(PresignedGetObjectRequest.class);
        HeadObjectResponse mockResponse = mock(HeadObjectResponse.class);

        when(mockedPresigned.url()).thenReturn(new URL("http://localhost:8080/download"));
        when(presigner.presignGetObject(any(Consumer.class))).thenReturn(mockedPresigned);
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        Map<String, URL> urls = videoService.generateDashUrls(bucket, mpdFile, segmentFiles);

        assertThat(urls).isNotNull();
        assertThat(urls).containsKeys("mpd", "seg1.mp4", "seg2.mp4");
        urls.values().forEach(url -> assertThat(url.toString()).isEqualTo("http://localhost:8080/download"));
    }
}