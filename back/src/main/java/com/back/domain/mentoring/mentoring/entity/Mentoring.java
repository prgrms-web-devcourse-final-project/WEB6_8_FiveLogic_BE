package com.back.domain.mentoring.mentoring.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class Mentoring extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(length = 255)
    private String thumb;
}
