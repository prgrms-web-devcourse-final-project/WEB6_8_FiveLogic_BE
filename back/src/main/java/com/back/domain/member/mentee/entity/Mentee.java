package com.back.domain.member.mentee.entity;

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

    @Column(name = "job_id")
    private Long jobId;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Mentee(Member member, Long jobId) {
        this.member = member;
        this.jobId = jobId;
        this.isDeleted = false;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
