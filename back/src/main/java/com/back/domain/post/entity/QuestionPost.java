package com.back.domain.post.entity;

import jakarta.persistence.Entity;

@Entity
public class QuestionPost extends Post{
    private Boolean isResolve;
}
