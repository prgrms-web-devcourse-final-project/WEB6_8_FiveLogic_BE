package com.back.domain.file.service;

import com.back.domain.file.entity.Video;
import com.back.domain.file.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final S3Presigner presigner;

    public Video createVideo(String transcodingStatus, String originalPath, String originalFilename, Integer duration, Long fileSize) {
        String uuid = UUID.randomUUID().toString();
        Video video = Video.create(uuid, transcodingStatus, originalPath, originalFilename, duration, fileSize);
        return videoRepository.save(video);
    }

    public URL generateUploadUrl(String bucket, String objectKey) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(30))
                        .putObjectRequest(request));

        return presignedRequest.url();
    }

    public URL generateDownloadUrl(String bucket, String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(builder -> builder
                        .signatureDuration(Duration.ofHours(1)) // 스트리밍 중 끊기지 않게 충분히
                        .getObjectRequest(request));

        return presignedRequest.url();
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
}
