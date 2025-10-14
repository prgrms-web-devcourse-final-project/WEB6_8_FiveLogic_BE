package com.back.domain.mentoring.mentoring.service;

import com.back.domain.mentoring.mentoring.error.ImageErrorCode;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class S3ImageUploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES  = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final String IMAGE_BASE_PATH = "images/";

    public String upload(MultipartFile file, String path) throws IOException {
        validateImageFile(file);

        String fullPath = IMAGE_BASE_PATH + path;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fullPath)
            .contentType(file.getContentType())
            .build();

        s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return s3Client.utilities()
            .getUrl(builder -> builder.bucket(bucket).key(fullPath))
            .toString();
    }

    private void validateImageFile(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException(ImageErrorCode.FILE_SIZE_EXCEEDED);
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException(ImageErrorCode.INVALID_FILE_TYPE);
        }
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ServiceException(ImageErrorCode.INVALID_FILE_TYPE);
        }
    }
}
