package com.back.domain.mentoring.mentoring.service;

import com.back.domain.mentoring.mentoring.error.ImageErrorCode;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ImageUploaderTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @InjectMocks
    private S3ImageUploader s3ImageUploader;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String IMAGE_BASE_PATH = "images/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3ImageUploader, "bucket", BUCKET_NAME);
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void upload_success() throws IOException {
        // given
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test-image-content".getBytes()
        );
        String path = "mentoring/123";
        String expectedKey = IMAGE_BASE_PATH + path;
        String expectedUrl = String.format("https://%s.s3.amazonaws.com/%s", BUCKET_NAME, expectedKey);

        // PutObjectRequest 객체를 캡처하기 위한 ArgumentCaptor 생성
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // putObject
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        // getUrl
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(ArgumentMatchers.<Consumer<GetUrlRequest.Builder>>any()))
            .thenReturn(URI.create(expectedUrl).toURL());

        // when
        String result = s3ImageUploader.upload(image, path);

        // then
        assertThat(result).isEqualTo(expectedUrl);

        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        // 캡처된 객체(실제 서비스에서 생성된 PutObjectRequest)의 내부 필드 검증
        PutObjectRequest capturedRequest = requestCaptor.getValue();

        // 버킷 이름, S3 Key Content-Type 검증
        assertThat(capturedRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(capturedRequest.key()).isEqualTo(expectedKey);
        assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("파일 크기 10MB 초과 시 예외")
    void upload_fail_fileSizeExceeded() {
        // given
        long exceedingSize = 10 * 1024 * 1024 + 1;
        MockMultipartFile largeFile = new MockMultipartFile(
            "image",
            "large.jpg",
            "image/jpeg",
            new byte[(int) exceedingSize]
        );

        // when & then
        assertThatThrownBy(() -> s3ImageUploader.upload(largeFile, "mentoring/123"))
            .isInstanceOf(ServiceException.class)
            .hasFieldOrPropertyWithValue("resultCode", ImageErrorCode.FILE_SIZE_EXCEEDED.getCode());

        // S3 putObject는 호출되지 않았는지 검증
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("허용되지 않은 파일 타입이면 예외")
    void upload_fail_invalidFileType() {
        // given
        MockMultipartFile textFile = new MockMultipartFile(
            "file",
            "doc.txt",
            "text/plain",
            "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3ImageUploader.upload(textFile, "mentoring/123"))
            .isInstanceOf(ServiceException.class)
            .hasFieldOrPropertyWithValue("resultCode", ImageErrorCode.INVALID_FILE_TYPE.getCode());

        // S3 putObject는 호출되지 않았는지 검증
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}