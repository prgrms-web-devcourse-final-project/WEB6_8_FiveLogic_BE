package com.back.domain.news.news.entity;

import com.back.domain.file.video.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.NewsComment;
import com.back.domain.news.like.entity.NewsLike;
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
    @ManyToOne
    private Member member;
    private String title;
    @OneToOne
    private Video video;
    private String content;
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsComment> newsComment;
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsLike> newsLikes = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private News(Member member, String title, Video video, String content, List<NewsComment> newsComment, List<NewsLike> newsLikes) {
        this.member = member;
        this.title = title;
        this.video = video;
        this.content = content;
        this.newsComment = newsComment;
        this.newsLikes = newsLikes;
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
                .newsComment(new ArrayList<>())
                .newsLikes(new ArrayList<>())
                .build();
    }

    public void like(NewsLike newsLike) {
        if (this.newsLikes.contains(newsLike)) {
            throw new IllegalStateException("이미 좋아요를 누른 뉴스입니다.");
        }
        this.newsLikes.add(newsLike);
    }

    /**
     * 좋아요를 취소할 수 있게 해야할까요?
     */
    @Deprecated(forRemoval = true)
    public void unLike(NewsLike newsLike) {
        if (!this.newsLikes.contains(newsLike)) {
            throw new IllegalStateException("좋아요를 누르지 않은 뉴스입니다.");
        }
        this.newsLikes.remove(newsLike);
    }

    public void addComment(NewsComment newsComment) {
        if (newsComment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        this.newsComment.add(newsComment);
    }

    public void removeComment(NewsComment newsComment) {
        if (newsComment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        this.newsComment.remove(newsComment);
    }

    public void update(String title, Video video, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (video == null) {
            throw new IllegalArgumentException("Video cannot be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        this.title = title;
        this.video = video;
        this.content = content;
    }
}
