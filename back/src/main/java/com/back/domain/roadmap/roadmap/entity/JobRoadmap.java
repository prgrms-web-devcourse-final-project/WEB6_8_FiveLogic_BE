package com.back.domain.roadmap.roadmap.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_roadmap")
@Getter @Setter
@NoArgsConstructor
public class JobRoadmap extends BaseEntity {
    @Column(name = "job_id", nullable = false)
    private Long jobId; // Job FK

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id") // RoadmapNode.roadmapId 참조
    @SQLRestriction("roadmap_type = 'JOB'")
    @OrderBy("stepOrder ASC")
    private List<RoadmapNode> nodes;

    public JobRoadmap(Long jobId) {
        this.jobId = jobId;
        this.nodes = new ArrayList<>();
    }

    public RoadmapNode getRootNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

}
