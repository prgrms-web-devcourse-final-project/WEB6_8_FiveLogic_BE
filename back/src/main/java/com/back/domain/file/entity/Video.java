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

/**
 * 영상파일을 저장하기위한 엔티티입니다.
 * BaseEntity를 상속받아 Id를 자동으로 생성합니다.
 * 사용자는 영상 요청을 위해 uuid를 사용합니다.
 * transcodingResults는 JSON형태로 저장되며, 영상의 다양한 해상도와 포맷에 대한 정보를 담습니다.
 * originalPath는 버킷명과 uuid를 조합하여 저장합니다.
 * views는 영상의 조회수를 나타냅니다.
 * originalFileName은 사용자가 업로드한 원본 파일명을 저장합니다.
 * duration은 영상의 길이를 초 단위로 저장합니다.
 * fileSize는 영상 파일의 크기를 바이트 단위로 저장합니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {
    @Column(unique = true)
    private String uuid;

    // @JdbcTypeCode(SqlTypes.JSON)을 통해 입력값이 JSON형태인지 검사합니다
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transcoding_results")
    private String transcodingResults;

    private String originalPath;

    private Integer views;

    private String originalFileName;

    private Integer duration;

    private Long fileSize;

    //빌터 패턴이 적용된 private 생성자를 통해 객체를 생성합니다.
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

    //정적 팩토리 메서드로 클래스에선언된 private한 빌더를 호출하여 값을 검증하고 객체를 생성합니다.
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
