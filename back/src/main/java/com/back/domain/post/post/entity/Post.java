package com.back.domain.post.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.entity.PostComment;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Post extends BaseEntity {
    @NotNull(message = "제목은 null일 수 없습니다.")
    private String title;
    @NotNull
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    public enum PostType {
        INFORMATIONPOST,
        PRACTICEPOST,
        QUESTIONPOST
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    private int viewCount;

    private Boolean isMento;
    private String carrer;

    private Boolean isResolve;



    public void addComment(PostComment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(PostComment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }


    public Boolean isAuthor( Member member) {
        return Objects.equals(this.member.getId(), member.getId());
    }

    public String getAuthorName() {
        return member.getName();
    }


    public static void validPostType(String postTypeStr) {
        try {
            Post.PostType.valueOf(postTypeStr);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("400-2", "유효하지 않은 PostType입니다.");
        }
    }
}
