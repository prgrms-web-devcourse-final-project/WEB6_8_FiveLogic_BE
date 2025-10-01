package com.back.domain.member.mentor.entity;

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

    @Column(name = "job_id")
    private Long jobId;

    @Column
    private Double rate;

    @Column(name = "career_years")
    private Integer careerYears;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Mentor(Member member, Long jobId, Double rate, Integer careerYears) {
        this.member = member;
        this.jobId = jobId;
        this.rate = rate;
        this.careerYears = careerYears;
        this.isDeleted = false;
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
