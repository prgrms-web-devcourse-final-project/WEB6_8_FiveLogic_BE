package com.back.domain.file.entity;

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

    private String originalPath;

    private Integer views;

    private String originalFileName;

    private Integer duration;

    private Long fileSize;

    @Builder(access = AccessLevel.PRIVATE)
    private Video(String uuid, String transcodingResults, String originalPath, Integer views, String originalFileName, Integer duration, Long fileSize) {
        this.uuid = uuid;
        this.transcodingResults = transcodingResults;
        this.originalPath = originalPath;
        this.views = views;
        this.originalFileName = originalFileName;
        this.duration = duration;
        this.fileSize = fileSize;
    }

    public static Video create(String uuid, String transcodingResults, String originalPath, String originalFileName, Integer duration, Long fileSize) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("uuid cannot be null or empty");
        }
        if (originalPath == null || originalPath.isBlank()) {
            throw new IllegalArgumentException("originalPath cannot be null or empty");
        }
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("originalFileName cannot be null or empty");
        }

        return Video.builder()
                .uuid(uuid)
                .transcodingResults(transcodingResults)
                .originalPath(originalPath)
                .views(0)
                .originalFileName(originalFileName)
                .duration(duration)
                .fileSize(fileSize)
                .build();
    }
}
