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
        // 노드 순회용 인덱스 (level, stepOrder 순 정렬)
        @Index(name = "idx_roadmap_composite", columnList = "roadmap_id, roadmap_type, level, step_order"),
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

    @Column(name = "level", nullable = false)
    private int level = 0; // 트리 깊이 (루트: 0, 자식: 부모 + 1)

    @Column(name = "raw_task_name")
    private String taskName; // Task 이름 표시값(DB에 없는 Task 입력시 입력값 그대로 출력)

    @Column(name = "learning_advice", columnDefinition = "TEXT")
    private String learningAdvice; // 학습 조언/방법

    @Column(name = "recommended_resources", columnDefinition = "TEXT")
    private String recommendedResources; // 추천 자료

    @Column(name = "learning_goals", columnDefinition = "TEXT")
    private String learningGoals; // 학습 목표

    @Column(name = "difficulty")
    private Integer difficulty; // 난이도 (1-5)

    @Column(name = "importance")
    private Integer importance; // 중요도 (1-5)

    @Column(name = "hours_per_day")
    private Integer hoursPerDay; // 하루 학습 시간 (시간 단위)

    @Column(name = "weeks")
    private Integer weeks; // 학습 주차

    @Column(name = "estimated_hours")
    private Integer estimatedHours; // 총 예상 학습 시간 (시간 단위)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task; // 표준 Task

    public enum RoadmapType {
        MENTOR, JOB
    }

    // Builder 패턴 적용된 생성자
    @Builder
    public RoadmapNode(String taskName, String learningAdvice, String recommendedResources,
                       String learningGoals, Integer difficulty, Integer importance,
                       Integer hoursPerDay, Integer weeks, Integer estimatedHours, Task task,
                       int stepOrder, int level, long roadmapId, RoadmapType roadmapType) {
        this.taskName = taskName;
        this.learningAdvice = learningAdvice;
        this.recommendedResources = recommendedResources;
        this.learningGoals = learningGoals;
        this.difficulty = difficulty;
        this.importance = importance;
        this.hoursPerDay = hoursPerDay;
        this.weeks = weeks;
        this.estimatedHours = estimatedHours;
        this.task = task;
        this.stepOrder = stepOrder;
        this.level = level;
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
        child.setLevel(this.level + 1); // 부모 level + 1로 자동 설정
    }

    public void setParent(RoadmapNode parent) {
        this.parent = parent;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public void setRoadmapId(Long roadmapId) {
        this.roadmapId = roadmapId;
    }

    public void setRoadmapType(RoadmapType roadmapType) {
        this.roadmapType = roadmapType;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
