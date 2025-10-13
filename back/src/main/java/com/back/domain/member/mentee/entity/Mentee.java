package com.back.domain.member.mentee.entity;

import com.back.domain.job.job.entity.Job;
import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Mentee extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Mentee(Member member, Job job) {
        this.member = member;
        this.job = job;
        this.isDeleted = false;
    }

    public void updateJob(Job job) {
        this.job = job;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public boolean isMember(Member member) {
        return this.member.equals(member);
    }
}
