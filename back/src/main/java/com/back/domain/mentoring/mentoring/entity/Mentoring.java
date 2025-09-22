package com.back.domain.mentoring.mentoring.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.global.converter.StringListConverter;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

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

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "JSON")
    private List<String> tags;

    @Column(length = 255)
    private String thumb;
}
