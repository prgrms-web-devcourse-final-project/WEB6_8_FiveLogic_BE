package com.back.domain.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private int viewCount;

    private int liked;

    private Boolean isMento;
    private String carrer;

    private Boolean isResolve;

}
