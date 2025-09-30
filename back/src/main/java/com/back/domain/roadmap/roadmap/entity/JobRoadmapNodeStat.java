package com.back.domain.roadmap.roadmap.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_roadmap_node_stat")
@Getter
@NoArgsConstructor
public class JobRoadmapNodeStat extends BaseEntity {
    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private RoadmapNode node;

    @Builder
    public JobRoadmapNodeStat(Integer stepOrder, Double weight, RoadmapNode node) {
        this.stepOrder = stepOrder;
        this.weight = weight != null ? weight : 0.0;
        this.node = node;
    }
}
