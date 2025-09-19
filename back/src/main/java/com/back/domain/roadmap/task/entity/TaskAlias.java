package com.back.domain.roadmap.task.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_alias")
@Getter @Setter
@NoArgsConstructor
public class TaskAlias extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name; // 사용자가 입력한 Task 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task; // 표준 Task 연결 (NULL이면 pending 상태)

    public TaskAlias(String name) {
        this.name = name;
        this.task = null; // 기본적으로 연결된 Task가 없음 (pending 상태)
    }
}
