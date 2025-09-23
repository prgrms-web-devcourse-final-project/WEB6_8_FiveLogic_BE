package com.back.domain.file.service;

import com.back.domain.file.entity.Video;
import com.back.domain.file.repository.VideoRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final S3Presigner presigner; //S3서비스로 부터 Presigned URL을 생성하기위한 클라이언트입니다.
    private final S3Client s3Client; // S3 API 호출을 위한 클라이언트입니다.

    /**
     * 비디오 엔티티를 생성합니다. 각 엔티티는 고유한 UUID값을 가지도록 UUID.randomUUID()를 통해 생성합니다.
     */
    public Video createVideo(String transcodingStatus, String originalPath, String originalFilename, Integer duration, Long fileSize) {
        String uuid = UUID.randomUUID().toString();
        Video video = Video.create(uuid, transcodingStatus, originalPath, originalFilename, duration, fileSize);
        return videoRepository.save(video);
    }

    /**
     * 비디오 엔티티를 UUID로 조회합니다. 존재하지 않는 UUID일 경우 NoSuchElementException을 발생시킵니다.
     */
    public Video getNewsByUuid(String uuid) {
        return videoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 비디오입니다."));
    }

    /**
     * 파일 업로드용 Presigned URL을 생성합니다.
     * 현재 코드에는 파일 존재 여부를 확인하는 로직이 포함되어 있습니다. 이는 잘못된 로직으로, feat/4브랜치에서 수정되었습니다. 이후 병합으로 반영될 예정입니다.
     */
    //HeadObjectRequest 고려
    public URL generateUploadUrl(String bucket, String objectKey) {
        if (!isExist(bucket, objectKey)) {
            throw new NoSuchElementException("요청한 파일이 존재하지 않습니다: " + objectKey);
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        // Presigned URL을 직접 생성하는 부분으로 서명은 30분간 유효합니다.
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

    /**
     * 파일 다운로드용 Presigned URL을 생성합니다. 다운로드 받기 전 HEAD 요청을 통해 파일 존재 여부를 확인하고, 존재하지 않을 경우 NoSuchElementException을 발생시킵니다.
     */
    public URL generateDownloadUrl(String bucket, String objectKey) {
        if (!isExist(bucket, objectKey)) {
            throw new NoSuchElementException("요청한 파일이 존재하지 않습니다: " + objectKey);
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        // Presigned URL을 직접 생성하는 부분으로 현재 영상정보의 특성상 서명은 1시간으로 길게 두었습니다.
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

    /**
     * DASH 스트리밍용 URL들을 생성합니다. MPD 파일과 각 세그먼트 파일들의 Presigned URL을 맵 형태로 반환합니다.
     */
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

    /**
     * S3 버킷 내에 특정 오브젝트가 존재하는지 확인합니다.
     */
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
