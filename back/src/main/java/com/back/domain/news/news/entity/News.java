package com.back.domain.news.news.entity;

import com.back.domain.file.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.like.entity.Like;
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
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private News(Member member, String title, Video video, String content, List<Comment> comment, List<Like> likes) {
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
                .likes(new ArrayList<>())
                .build();
    }

    public void like(Like like) {
        if (this.likes.contains(like)) {
            throw new IllegalStateException("이미 좋아요를 누른 뉴스입니다.");
        }
        this.likes.add(like);
    }

    public void unLike(Like like) {
        if (!this.likes.contains(like)) {
            throw new IllegalStateException("좋아요를 누르지 않은 뉴스입니다.");
        }
        this.likes.remove(like);
    }

    public void addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        this.comment.add(comment);
    }

    public void removeComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        this.comment.remove(comment);
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
