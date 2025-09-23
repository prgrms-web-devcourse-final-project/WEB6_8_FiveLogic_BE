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
/**
 * Table어노테이션을 사용해 복합 유니크 제약조건 설정 합니다. 한 멤버는 동일한 뉴스에 대해 하나의 좋아요만 가질 수 있음
 */

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
