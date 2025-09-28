package com.back.domain.file.video.service;

import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner presigner;
    private final S3Client s3Client;

    public URL generateUploadUrl(String bucket, String objectKey, Integer expireMinutes) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(expireMinutes))
                        .putObjectRequest(request));

        if (presignedRequest == null) {
            throw new ServiceException("500", "Presigned URL 생성 실패");
        }

        URL url = presignedRequest.url();

        return url;
    }

    public URL generateUploadUrl(String bucket, String objectKey) {
        validateRequest(bucket, objectKey);

        return generateUploadUrl(bucket, objectKey, 30);
    }

    public URL generateDownloadUrl(String bucket, String objectKey, Integer expireHours) {
        isExist(bucket, objectKey);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(expireHours))
                        .getObjectRequest(request));

        if (presignedRequest == null) {
            throw new ServiceException("500", "Presigned URL 생성 실패");
        }

        URL url = presignedRequest.url();

        return url;
    }

    public URL generateDownloadUrl(String bucket, String objectKey) {
        validateRequest(bucket, objectKey);

        return generateDownloadUrl(bucket, objectKey, 60);
    }

    public void isExist(String bucket, String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
        } catch (NoSuchKeyException e) {
            throw new ServiceException("404", "요청한 파일이 존재하지 않습니다: " + objectKey);
        } catch (S3Exception e) {
            throw new ServiceException("500", "파일 존재 여부 확인 중 오류가 발생했습니다.");
        }
    }

    public void validateRequest(String bucket, String objectKey) {
        if (bucket == null || bucket.isEmpty() || objectKey == null || objectKey.isEmpty()) {
            throw new ServiceException("400", "버킷 이름과 객체 키는 필수입니다.");
        }
    }
}
