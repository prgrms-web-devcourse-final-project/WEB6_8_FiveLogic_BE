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

    // ---- 추가 통계 필드 ----
    @Column(name = "average_position")
    private Double averagePosition; // 각 노드가 멘토 로드맵에서 평균적으로 위치한 인덱스(1..N)

    @Column(name = "mentor_count")
    private Integer mentorCount; // 몇 명의 멘토 로드맵에 등장했는지 (unique mentor count)

    @Column(name = "outgoing_transitions")
    private Integer outgoingTransitions; // 이 노드에서 다른 노드로 이동한 총 전이수

    @Column(name = "incoming_transitions")
    private Integer incomingTransitions; // 타 노드에서 이 노드로 들어오는 전이수

    @Column(name = "transition_counts", columnDefinition = "TEXT")
    private String transitionCounts; // (선택) JSON 직렬화: { "T:5":3, "T:7":1 } 형태로 보관 가능

    @Builder
    public JobRoadmapNodeStat(Integer stepOrder, Double weight, RoadmapNode node) {
        this.stepOrder = stepOrder;
        this.weight = weight != null ? weight : 0.0;
        this.node = node;
    }
}
