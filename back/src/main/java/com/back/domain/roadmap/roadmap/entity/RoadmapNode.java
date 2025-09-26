package com.back.domain.roadmap.roadmap.entity;

import com.back.domain.roadmap.task.entity.Task;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "roadmap_node")
@Getter @Setter
@NoArgsConstructor
public class RoadmapNode extends BaseEntity {
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

    public RoadmapNode(String rawTaskName, String description, Task task) {
        this.rawTaskName = rawTaskName;
        this.description = description;
        this.task = task;
    }

    public void addChild(RoadmapNode child) {
        children.add(child);
        child.setParent(this);
    }
}
