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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "roadmap_id")
    @SQLRestriction("roadmap_type = 'MENTOR'")
    @OrderBy("stepOrder ASC")
    private List<RoadmapNode> nodes;


    public MentorRoadmap(Long mentorId, String title, String description) {
        this.mentorId = mentorId;
        this.title = title;
        this.description = description;
        this.nodes = new ArrayList<>();
    }

    public RoadmapNode getRootNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    // 노드 추가 헬퍼 메서드 (저장 후 사용)
    public void addNode(RoadmapNode node) {
        if (this.getId() != null) {
            node.setRoadmapId(this.getId());
        }
        node.setRoadmapType(RoadmapNode.RoadmapType.MENTOR);
        this.nodes.add(node);
    }

    // 여러 노드 일괄 추가
    public void addNodes(List<RoadmapNode> nodes) {
        nodes.forEach(this::addNode);
    }

    // 저장 후 roadmapId 설정 (cascade 사용 시 필요)
    public void updateNodesWithRoadmapId() {
        if (this.getId() != null) {
            nodes.forEach(node -> node.setRoadmapId(this.getId()));
        }
    }

}
