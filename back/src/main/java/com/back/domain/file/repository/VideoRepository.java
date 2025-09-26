package com.back.domain.file.repository;

import com.back.domain.file.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {
    Optional<Video> findByUuid(String uuid);
}
