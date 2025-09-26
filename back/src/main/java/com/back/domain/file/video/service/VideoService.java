package com.back.domain.file.video.service;

import com.back.domain.file.video.entity.Video;
import com.back.domain.file.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;

    public Video createVideo(String uuid, String transcodingStatus, String originalPath, Integer duration, Long fileSize) {
        Video video = Video.create(uuid, transcodingStatus, originalPath, duration, fileSize);
        return videoRepository.save(video);
    }

    public Video getNewsByUuid(String uuid) {
        return videoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 비디오입니다."));
    }
}
