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
    private String status;

    private String path;

    private Integer duration;

    private Long fileSize;

    @Builder(access = AccessLevel.PRIVATE)
    private Video(String uuid, String status, String path, Integer duration, Long fileSize) {
        this.uuid = uuid;
        this.status = status;
        this.path = path;
        this.duration = duration;
        this.fileSize = fileSize;
    }

    public static Video create(String uuid, String status, String path, Integer duration, Long fileSize) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("uuid cannot be null or empty");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be null or empty");
        }

        return Video.builder()
                .uuid(uuid)
                .status(status)
                .path(path)
                .duration(duration)
                .fileSize(fileSize)
                .build();
    }
    public void updateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status cannot be null or empty");
        }
        this.status = status;
    }
}
