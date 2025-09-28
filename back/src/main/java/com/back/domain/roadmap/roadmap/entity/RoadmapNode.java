package com.back.domain.roadmap.roadmap.entity;

import com.back.domain.roadmap.task.entity.Task;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private List<RoadmapNode> children = new ArrayList<>();

    @Column(name = "step_order", nullable = false)
    private int stepOrder = 0;

    @Column(name = "raw_task_name")
    private String taskName; // Task 이름 표시값(DB에 없는 Task 입력시 입력값 그대로 출력)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task; // 표준 Task

    public enum RoadmapType {
        MENTOR, JOB
    }

    // Builder 패턴 적용된 생성자
    @Builder
    public RoadmapNode(String taskName, String description, Task task, int stepOrder, long roadmapId, RoadmapType roadmapType) {
        this.taskName = taskName;
        this.description = description;
        this.task = task;
        this.stepOrder = stepOrder;
        this.roadmapId = roadmapId;
        this.roadmapType = roadmapType;
    }


    public void addChild(RoadmapNode child) {
        if (child == null) {
            throw new IllegalArgumentException("자식 노드는 null일 수 없습니다.");
        }
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    private void setParent(RoadmapNode parent) {
        this.parent = parent;
    }
}
