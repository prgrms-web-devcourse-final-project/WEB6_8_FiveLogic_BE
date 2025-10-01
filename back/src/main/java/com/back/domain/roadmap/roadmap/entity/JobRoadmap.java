package com.back.domain.roadmap.roadmap.entity;

import com.back.domain.job.job.entity.Job;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_roadmap")
@Getter
@NoArgsConstructor
public class JobRoadmap extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "roadmap_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @SQLRestriction("roadmap_type = 'JOB'")
    @OrderBy("level ASC, stepOrder ASC")
    private List<RoadmapNode> nodes;

    @Builder
    public JobRoadmap(Job job, List<RoadmapNode> nodes) {
        this.job = job;
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    public RoadmapNode getRootNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

}
