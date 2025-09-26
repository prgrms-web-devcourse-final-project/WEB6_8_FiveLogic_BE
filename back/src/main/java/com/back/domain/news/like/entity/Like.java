package com.back.domain.news.like.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "news_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "news_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private News news;

    @Builder(access = AccessLevel.PRIVATE)
    private Like(Member member, News news) {
        this.member = member;
        this.news = news;
    }

    public static Like create(Member member, News news) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        if (news == null) {
            throw new IllegalArgumentException("News cannot be null");
        }
        Like like = Like.builder()
                .member(member)
                .news(news)
                .build();
        return like;
    }
}
