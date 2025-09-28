package com.back.domain.file.video.service;

import com.back.domain.file.video.entity.Video;
import com.back.domain.file.video.repository.VideoRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new ServiceException("404","Video not found"));
    }

    public Video updateStatus(String uuid, String status){
        Video news = getNewsByUuid(uuid);
        if (status == null|| status.isBlank()) {
            throw new ServiceException("400","status cannot be null or empty");
        }
        news.updateStatus(status);
        return videoRepository.save(news);
    }
}
