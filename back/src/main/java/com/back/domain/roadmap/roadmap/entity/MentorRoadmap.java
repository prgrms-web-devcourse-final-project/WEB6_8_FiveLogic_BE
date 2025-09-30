package com.back.domain.roadmap.roadmap.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentor_roadmap")
@Getter
@NoArgsConstructor
public class MentorRoadmap extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "roadmap_id")
    @SQLRestriction("roadmap_type = 'MENTOR'")
    @OrderBy("stepOrder ASC")
    private List<RoadmapNode> nodes;


    public MentorRoadmap(Mentor mentor, String title, String description) {
        this.mentor = mentor;
        this.title = title;
        this.description = description;
        this.nodes = new ArrayList<>();
    }

    public RoadmapNode getRootNode() {
        return nodes.stream()
                .filter(node -> node.getStepOrder() == 1)
                .findFirst()
                .orElse(null);
    }

    // 노드 추가 헬퍼 메서드 (이미 완전히 초기화된 노드 추가)
    public void addNode(RoadmapNode node) {
        if (node == null) {
            throw new IllegalArgumentException("추가할 노드는 null일 수 없습니다.");
        }
        // 노드는 이미 생성자에서 완전히 초기화되어 전달됨
        this.nodes.add(node);
    }

    // 여러 노드 일괄 추가
    public void addNodes(List<RoadmapNode> nodes) {
        nodes.forEach(this::addNode);
    }


    // 제목 수정 (비즈니스 로직)
    public void updateTitle(String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("로드맵 제목은 필수입니다.");
        }
        this.title = newTitle.trim();
    }

    // 설명 수정 (비즈니스 로직)
    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public void clearNodes() {
        this.nodes.clear();
    }
}
