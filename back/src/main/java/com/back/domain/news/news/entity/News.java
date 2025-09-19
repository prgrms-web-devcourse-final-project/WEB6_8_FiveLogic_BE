package com.back.domain.news.news.entity;

import com.back.domain.file.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.Comment;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    private String title;
    @OneToOne
    private Video video;
    private String content;
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comment;
    private Integer likes;

    @Builder(access = AccessLevel.PRIVATE)
    private News(Member member, String title, Video video, String content, List<Comment> comment, Integer likes) {
        this.member = member;
        this.title = title;
        this.video = video;
        this.content = content;
        this.comment = comment;

        this.likes = likes;
    }

    public static News create(Member member, String title, Video video, String content) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (video == null) {
            throw new IllegalArgumentException("Video cannot be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        return News.builder()
                .member(member)
                .title(title)
                .video(video)
                .content(content)
                .comment(new ArrayList<>())
                .likes(0)
                .build();
    }
}
