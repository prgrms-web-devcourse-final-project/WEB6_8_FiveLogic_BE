package com.back.domain.news.news.entity;

import com.back.domain.news.comment.entity.Comment;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News extends BaseEntity {
    private String title;
    //todo: video entity 생성 후 통합 필요
//    @OneToOne
//    private Video video;
    private String content;
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comment;
    private Integer likes = 0;

    @Builder(access = AccessLevel.PRIVATE)
    private News(String title, String content, Integer likes) {
        this.title = title;
        this.content = content;
        this.likes = likes;
    }

    public static News create(String title, String content) {
        return News.builder()
                .title(title)
                .content(content)
                .likes(0)
                .build();
    }
}
