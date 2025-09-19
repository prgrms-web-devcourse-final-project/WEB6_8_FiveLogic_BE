package com.back.domain.post.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Post extends BaseEntity {
    private String title;
    private String content;
    private String authorName;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    public enum PostType {
        INFORMATIONPOST,
        PRACTICEPOST,
        QUESTIONPOST
    }

    private int viewCount;

    private int liked;

}
