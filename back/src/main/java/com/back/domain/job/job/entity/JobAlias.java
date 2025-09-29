package com.back.domain.job.job.entity;

import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_alias")
@Getter
@NoArgsConstructor
public class JobAlias extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name; // 사용자가 입력한 직군 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job; // 표준 Job 연결 (NULL이면 pending 상태)

    public JobAlias(String name) {
        this.name = name;
        this.job = null; // 기본적으로 연결된 Job이 없음 (pending 상태)
    }

    public void linkToJob(Job job) {
        this.job = job;
    }

    public boolean isPending() {
        return this.job == null;
    }
}
