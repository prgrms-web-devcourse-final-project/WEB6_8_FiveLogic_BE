package com.back.domain.post.comment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PostComment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String role;

}
