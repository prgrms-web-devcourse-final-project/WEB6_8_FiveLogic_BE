package com.back.domain.roadmap.roadmap.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_roadmap_node_stat")
@Getter @Setter
@NoArgsConstructor
public class JobRoadmapNodeStat extends BaseEntity {
    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "weight", nullable = false)
    private Double weight = 0.0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private RoadmapNode node;
}
