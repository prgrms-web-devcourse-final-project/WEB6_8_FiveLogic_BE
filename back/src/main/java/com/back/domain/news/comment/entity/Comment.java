package com.back.domain.news.comment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {
    @ManyToOne
    private Member member;
    @ManyToOne
    @JoinColumn
    private News news;
    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    private Comment(Member member, News news, String content) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        if (news == null) {
            throw new IllegalArgumentException("News cannot be null");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        this.member = member;
        this.news = news;
        this.content = content;
    }

    public static Comment create(Member member, News news, String content) {
        return Comment.builder()
                .member(member)
                .news(news)
                .content(content)
                .build();
    }
}
