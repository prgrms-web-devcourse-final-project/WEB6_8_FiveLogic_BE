package com.back.domain.roadmap.roadmap.entity;

import com.back.domain.roadmap.task.entity.Task;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "roadmap_node", indexes = {
        // 노드 순회용 인덱스
        @Index(name = "idx_roadmap_composite", columnList = "roadmap_id, roadmap_type, step_order"),
        @Index(name = "idx_roadmap_parent", columnList = "roadmap_id, roadmap_type, parent_id")
})
@Getter
@NoArgsConstructor
public class RoadmapNode extends BaseEntity {
    @Column(name = "roadmap_id", nullable = false)
    private Long roadmapId;

    @Enumerated(EnumType.STRING)
    @Column(name = "roadmap_type", nullable = false)
    private RoadmapType roadmapType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RoadmapNode parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoadmapNode> children;

    @Column(name = "step_order", nullable = false)
    private int stepOrder = 0;

    @Column(name = "raw_task_name")
    private String rawTaskName; // 원본 Task 입력값

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task; // 표준 Task

    public enum RoadmapType {
        MENTOR, JOB
    }

    public RoadmapNode(String rawTaskName, String description, Task task) {
        this.rawTaskName = rawTaskName;
        this.description = description;
        this.task = task;
    }

    public void addChild(RoadmapNode child) {
        children.add(child);
        child.setParent(this);
    }

    // 현재 사용되는 setter들을 대체하는 메서드들
    public void setRoadmapId(Long roadmapId) {
        this.roadmapId = roadmapId;
    }

    public void setRoadmapType(RoadmapType roadmapType) {
        this.roadmapType = roadmapType;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public void setParent(RoadmapNode parent) {
        this.parent = parent;
    }
}
