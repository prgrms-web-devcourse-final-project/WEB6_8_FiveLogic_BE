package com.back.domain.mentoring.reservation.entity;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.global.exception.ServiceException;
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

    private void updateStatus(ReservationStatus status) {
        this.status = status;

        // 양방향 동기화
        if (status.equals(ReservationStatus.CANCELED) || status.equals(ReservationStatus.REJECTED)) {
            mentorSlot.removeReservation();
        } else {
            mentorSlot.updateStatus();
        }
    }

    public boolean isMentor(Mentor mentor) {
        return this.mentor.equals(mentor);
    }

    public boolean isMentee(Mentee mentee) {
        return this.mentee.equals(mentee);
    }

    public void approve(Mentor mentor) {
        ensureMentor(mentor);
        ensureCanApprove();
        ensureNotPast();
        updateStatus(ReservationStatus.APPROVED);
    }

    public void reject(Mentor mentor) {
        ensureMentor(mentor);
        ensureCanReject();
        ensureNotPast();
        updateStatus(ReservationStatus.REJECTED);
    }

    public void cancel(Mentor mentor) {
        ensureMentor(mentor);
        ensureCanCancel();
        ensureNotPast();
        updateStatus(ReservationStatus.CANCELED);
    }

    public void cancel(Mentee mentee) {
        ensureMentee(mentee);
        ensureCanCancel();
        ensureNotPast();
        updateStatus(ReservationStatus.CANCELED);
    }


    // ===== 헬퍼 메서드 =====

    private void ensureMentor(Mentor mentor) {
        if (!isMentor(mentor)) {
            throw new ServiceException(ReservationErrorCode.FORBIDDEN_NOT_MENTOR);
        }
    }

    private void ensureMentee(Mentee mentee) {
        if (!isMentee(mentee)) {
            throw new ServiceException(ReservationErrorCode.FORBIDDEN_NOT_MENTEE);
        }
    }

    private void ensureCanApprove() {
        if(!this.status.canApprove()) {
            throw new ServiceException(ReservationErrorCode.CANNOT_APPROVE);
        }
    }

    private void ensureCanReject() {
        if(!this.status.canReject()) {
            throw new ServiceException(ReservationErrorCode.CANNOT_REJECT);
        }
    }

    private void ensureCanCancel() {
        if(!this.status.canCancel()) {
            throw new ServiceException(ReservationErrorCode.CANNOT_CANCEL);
        }
    }

    private void ensureNotPast() {
        if (mentorSlot.isPast()) {
            throw new ServiceException(ReservationErrorCode.INVALID_MENTOR_SLOT);
        }
    }
}
