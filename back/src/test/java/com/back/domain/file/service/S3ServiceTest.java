package com.back.domain.file.service;

import com.back.domain.file.video.service.S3Service;
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
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {
    @Mock
    private S3Presigner presigner;
    @Mock
    private S3Client s3Client;
    @InjectMocks
    private S3Service s3Service;


    @Test
    @DisplayName("S3 업로드 URL 생성")
    void generateUploadUrlTest() throws MalformedURLException {
        String bucket = "test-bucket";
        String objectKey = "test-video.mp4";

        PresignedPutObjectRequest mocked = mock(PresignedPutObjectRequest.class);

        when(presigner.presignPutObject(any(Consumer.class))).thenReturn(mocked);

        when(mocked.url()).thenReturn(new URL("http://localhost:8080/upload"));

        URL url = s3Service.generateUploadUrl(bucket, objectKey);

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

        URL url = s3Service.generateDownloadUrl(bucket, objectKey);

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
            s3Service.generateUploadUrl(bucket, objectKey);
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
            s3Service.generateDownloadUrl(bucket, objectKey);
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

        boolean exist = s3Service.isExist(bucket, objectKey);

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("isExist() - 객체 존재하지 않으면 false 반환")
    void isExist_objectNotExists_shouldReturnFalse() {
        String bucket = "test-bucket";
        String objectKey = "non-existing.mp4";

        doThrow(S3Exception.builder().statusCode(404).build())
                .when(s3Client).headObject(any(HeadObjectRequest.class));

        boolean exist = s3Service.isExist(bucket, objectKey);

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

        Map<String, URL> urls = s3Service.generateDashUrls(bucket, mpdFile, segmentFiles);

        assertThat(urls).isNotNull();
        assertThat(urls).containsKeys("mpd", "seg1.mp4", "seg2.mp4");
        urls.values().forEach(url -> assertThat(url.toString()).isEqualTo("http://localhost:8080/download"));
    }
}
