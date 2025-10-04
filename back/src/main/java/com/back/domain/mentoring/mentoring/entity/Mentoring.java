package com.back.domain.mentoring.mentoring.entity;

import com.back.domain.member.mentor.entity.Mentor;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToMany(mappedBy = "mentoring", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MentoringTag> mentoringTags = new ArrayList<>();

    @Column(length = 255)
    private String thumb;

    @Column
    private double rating = 0.0;

    @Builder
    public Mentoring(Mentor mentor, String title, String bio, String thumb) {
        this.mentor = mentor;
        this.title = title;
        this.bio = bio;
        this.thumb = thumb;
    }

    public void update(String title, String bio, List<Tag> tags, String thumb) {
        this.title = title;
        this.bio = bio;
        this.thumb = thumb;

        updateTags(tags);
    }

    public void updateTags(List<Tag> tags) {
        this.mentoringTags.clear();

        if (tags != null) {
            tags.forEach(tag ->
                this.mentoringTags.add(new MentoringTag(this, tag))
            );
        }
    }

    public void updateRating(double averageRating) {
        this.rating = averageRating;
    }

    public boolean isOwner(Mentor mentor) {
        return this.mentor.equals(mentor);
    }

    public List<String> getTagNames() {
        return mentoringTags.stream()
            .map(mentoringTag -> mentoringTag.getTag().getName())
            .toList();
    }
}
