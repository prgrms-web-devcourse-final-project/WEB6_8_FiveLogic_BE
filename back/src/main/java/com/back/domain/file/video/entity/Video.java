package com.back.domain.file.video.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {
    @Column(unique = true)
    private String uuid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transcoding_results")
    private String transcodingResults;

    private String path;

    private Integer duration;

    private Long fileSize;

    @Builder(access = AccessLevel.PRIVATE)
    private Video(String uuid, String transcodingResults, String path, Integer duration, Long fileSize) {
        this.uuid = uuid;
        this.transcodingResults = transcodingResults;
        this.path = path;
        this.duration = duration;
        this.fileSize = fileSize;
    }

    public static Video create(String uuid, String transcodingResults, String path, Integer duration, Long fileSize) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("uuid cannot be null or empty");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be null or empty");
        }

        return Video.builder()
                .uuid(uuid)
                .transcodingResults(transcodingResults)
                .path(path)
                .duration(duration)
                .fileSize(fileSize)
                .build();
    }
}
