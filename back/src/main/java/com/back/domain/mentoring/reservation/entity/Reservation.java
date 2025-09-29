package com.back.domain.mentoring.reservation.entity;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
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

    @Builder
    public Reservation(Mentoring mentoring, Mentee mentee, MentorSlot mentorSlot, String preQuestion) {
        this.mentoring = mentoring;
        this.mentor = mentoring.getMentor();
        this.mentee = mentee;
        this.mentorSlot = mentorSlot;
        this.preQuestion = preQuestion;
        this.status = ReservationStatus.PENDING;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;

        // 양방향 동기화
        if (status.equals(ReservationStatus.CANCELED) || status.equals(ReservationStatus.REJECTED)) {
            mentorSlot.removeReservation();
        } else {
            mentorSlot.updateStatus();
        }
    }

    public boolean isMentee(Mentee mentee) {
        return this.mentee.equals(mentee);
    }
}
