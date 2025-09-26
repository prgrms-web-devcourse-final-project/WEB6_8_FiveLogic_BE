package com.back.domain.roadmap.roadmap.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_roadmap")
@Getter @Setter
@NoArgsConstructor
public class JobRoadmap extends BaseEntity {
    @Column(name = "job_id", nullable = false)
    private Long jobId; // Job FK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_node_id", nullable = false)
    private RoadmapNode rootNode;

    public JobRoadmap(Long jobId, RoadmapNode rootNode) {
        this.jobId = jobId;
        this.rootNode = rootNode;
    }
}
