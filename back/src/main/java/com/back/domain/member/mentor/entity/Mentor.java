package com.back.domain.member.mentor.entity;

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
public class Mentor extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column
    private Double rate;

    @Column(name = "career_years")
    private Integer careerYears;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Mentor(Member member, Job job, Double rate, Integer careerYears) {
        this.member = member;
        this.job = job;
        this.rate = rate;
        this.careerYears = careerYears;
        this.isDeleted = false;
    }

    public void updateJob(Job job) {
        this.job = job;
    }

    public void updateCareerYears(Integer careerYears) {
        this.careerYears = careerYears;
    }

    public void updateRating(Double averageRating) {
        this.rate = averageRating;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public boolean isMember(Member member) {
        return this.member.equals(member);
    }
}
