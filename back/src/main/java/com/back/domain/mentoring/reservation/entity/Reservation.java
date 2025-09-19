package com.back.domain.mentoring.reservation.entity;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Reservation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_id", nullable = false)
    private Mentoring mentoring;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_slot_id", nullable = false)
    private MentorSlot mentorSlot;

    @Column(columnDefinition = "TEXT")
    private String preQuestion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
}
