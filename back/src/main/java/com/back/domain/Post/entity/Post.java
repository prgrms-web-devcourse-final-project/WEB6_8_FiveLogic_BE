package com.back.domain.Post.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
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

    private enum postType {
        informationPost, practicePost, questionPost
    }

    private int viewCount;

    private int liked;
}
