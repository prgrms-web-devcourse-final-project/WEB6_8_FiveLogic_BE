package com.back.domain.file.video.service;

import com.back.domain.file.video.entity.Video;
import com.back.domain.file.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;


    public Video createVideo(String transcodingStatus, String originalPath, String originalFilename, Integer duration, Long fileSize) {
        String uuid = UUID.randomUUID().toString();
        Video video = Video.create(uuid, transcodingStatus, originalPath, originalFilename, duration, fileSize);
        return videoRepository.save(video);
    }

    public Video getNewsByUuid(String uuid) {
        return videoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 비디오입니다."));
    }
}
