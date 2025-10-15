package com.back.domain.post.comment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@Entity
@Getter
public class PostComment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String role;

    private Boolean isAdopted;

    public Boolean isAuthor( Member member) {
        return Objects.equals(this.member.getId(), member.getId());
    }

    public String getAuthorName() {
        return member.getNickname();
    }

    public Long getAuthorId() {
        return member.getId();
    }

    @Builder
    public PostComment(Post post, String content, Member member, String role) {
        this.post = post;
        this.content = content;
        this.member = member;
        this.role = role;
        this.isAdopted = false;
    }


    public void updateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글을 입력해주세요");
        }
        this.content = content;
    }

    public void updatePost(Post post) {
        this.post = post;
    }

    public void adoptComment() {
        this.isAdopted = true;
    }

}
