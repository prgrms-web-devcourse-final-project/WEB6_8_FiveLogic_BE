package com.back.domain.post.entity;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class PracticePost extends Post{
    private Boolean isMento;
    private String carrer;
}
