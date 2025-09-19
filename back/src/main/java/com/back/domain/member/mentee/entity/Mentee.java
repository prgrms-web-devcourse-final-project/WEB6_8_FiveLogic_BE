package com.back.domain.member.mentee.entity;

import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Mentee extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "job_id")
    private Long jobId;

    public Mentee(Member member, Long jobId) {
        this.member = member;
        this.jobId = jobId;
    }
}
