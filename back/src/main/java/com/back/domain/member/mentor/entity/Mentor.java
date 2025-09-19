package com.back.domain.member.mentor.entity;

import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
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

    public Mentor(Member member, Long jobId, Double rate, Integer careerYears) {
        this.member = member;
        this.jobId = jobId;
        this.rate = rate;
        this.careerYears = careerYears;
    }
}
