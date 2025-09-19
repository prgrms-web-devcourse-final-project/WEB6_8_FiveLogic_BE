package com.back.domain.roadmap.roadmap.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mentor_roadmap")
@Getter @Setter
@NoArgsConstructor
public class MentorRoadmap extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId; // Mentor 엔티티 FK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_node_id", nullable = false)
    private RoadmapNode rootNode;

    public MentorRoadmap(Long mentorId, String title, String description, RoadmapNode rootNode) {
        this.mentorId = mentorId;
        this.title = title;
        this.description = description;
        this.rootNode = rootNode;
    }
}
