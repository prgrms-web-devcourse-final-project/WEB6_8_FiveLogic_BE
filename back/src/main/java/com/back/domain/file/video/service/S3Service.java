package com.back.domain.file.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner presigner;
    private final S3Client s3Client;

    public URL generateUploadUrl(String bucket, String objectKey) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(30))
                        .putObjectRequest(request));

        URL url = presignedRequest.url();
        if (url == null) {
            throw new RuntimeException("Presigned URL 생성 실패");
        }

        return url;
    }

    public URL generateDownloadUrl(String bucket, String objectKey) {
        if (!isExist(bucket, objectKey)) {
            throw new NoSuchElementException("요청한 파일이 존재하지 않습니다: " + objectKey);
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(builder -> builder
                        .signatureDuration(Duration.ofHours(1))
                        .getObjectRequest(request));

        URL url = presignedRequest.url();
        if (url == null) {
            throw new RuntimeException("Presigned URL 생성 실패");
        }

        return url;
    }

    // DASH용 인덱스 + 세그먼트 URL 발급
    public Map<String, URL> generateDashUrls(String bucket, String mpdFile, List<String> segmentFiles) {
        // MPD 파일 URL
        URL mpdUrl = generateDownloadUrl(bucket, mpdFile);

        // 각 세그먼트 파일 URL
        Map<String, URL> segmentUrls = segmentFiles.stream()
                .collect(Collectors.toMap(f -> f, f -> generateDownloadUrl(bucket, f)));

        // MPD 포함 합쳐서 반환
        segmentUrls.put("mpd", mpdUrl);
        return segmentUrls;
    }

    public boolean isExist(String bucket, String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }
}
