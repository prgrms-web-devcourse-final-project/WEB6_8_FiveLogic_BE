package com.back.domain.post.like.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "post_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @Enumerated(EnumType.STRING)
    private LikeStatus status;

    public enum LikeStatus {
        LIKE, DISLIKE
    }

    @Builder(access = AccessLevel.PRIVATE)
    private PostLike(Member member, Post post, LikeStatus status) {
        this.member = member;
        this.post = post;
        this.status = status;
    }

    public static PostLike create(Member member, Post post, LikeStatus status) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return PostLike.builder()
                .member(member)
                .post(post)
                .status(status)
                .build();
    }

    @Transactional
    public void updateStatus(LikeStatus status) {


        this.status = status;
    }
}
