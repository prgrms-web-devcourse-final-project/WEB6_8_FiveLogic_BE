package com.back.domain.post.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.entity.PostComment;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
public class Post extends BaseEntity {
    @NotNull(message = "제목은 null일 수 없습니다.")
    private String title;

    @NotNull
    @Column(columnDefinition = "TEXT")
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
    private String job;

    private Boolean isResolve;

    @Builder
    public Post(String title, String content, Member member, PostType postType, List<PostComment> comments) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.postType = postType;
        this.comments = comments != null ? comments : new ArrayList<>();
        this.viewCount = 0;
    }



    public void addComment(PostComment comment) {
        comments.add(comment);
        comment.updatePost(this);
    }

    public void removeComment(PostComment comment) {
        comments.remove(comment);
        comment.updatePost(null);
    }


    public Boolean isAuthor( Member member) {
        return Objects.equals(this.member.getId(), member.getId());
    }

    public String getAuthorName() {
        return member.getNickname();
    }


    public static void validPostType(String postTypeStr) {
        try {
            Post.PostType.valueOf(postTypeStr);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("400", "유효하지 않은 PostType입니다.");
        }
    }

    public void updateTitle(String title) {
        if(title == null || title.isBlank()) {
            throw new ServiceException("400", "제목은 null이거나 공백일 수 없습니다.");
        }
        this.title = title;
    }

    public void updateContent(String content) {
        if(content == null || content.isBlank()) {
            throw new ServiceException("400", "내용은 null이거나 공백일 수 없습니다.");
        }
        this.content = content;
    }

    public void increaseViewCount() {
        this.viewCount ++;
    }

    public void updateResolveStatus(Boolean isResolve) {
        this.isResolve = isResolve;
    }

    public void updateJob(String job) {
        if (job == null || job.isBlank()) {
            throw new ServiceException("400", "직업은 null이거나 공백일 수 없습니다.");
        }
        this.job = job;
    }
}
