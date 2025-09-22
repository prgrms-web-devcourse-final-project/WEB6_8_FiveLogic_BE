package com.back.domain.mentoring.mentoring.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.global.converter.StringListConverter;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
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

    @Builder
    public Mentoring(Mentor mentor, String title, String bio, List<String> tags, String thumb) {
        this.mentor = mentor;
        this.title = title;
        this.bio = bio;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.thumb = thumb;
    }
}
