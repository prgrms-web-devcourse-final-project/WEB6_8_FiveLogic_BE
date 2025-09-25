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

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Post extends BaseEntity {
    @NotNull(message = "제목은 null일 수 없습니다.")
    private String title;
    @NotNull
    private String content;
    @NotNull
    private String authorName;

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

    private int liked;

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


    public Boolean checkAuthority(Post post, Member member) {
        boolean eq = true;
        Long authorId = post.getMember().getId();
        if (authorId != member.getId()) eq = false;
        return eq;
    }

    public static void validPostType(String postTypeStr) {
        try {
            Post.PostType.valueOf(postTypeStr);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("400-2", "유효하지 않은 PostType입니다.");
        }
    }
}
