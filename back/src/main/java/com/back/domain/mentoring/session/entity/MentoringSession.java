package com.back.domain.mentoring.session.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MentoringSession extends BaseEntity {
    private String sessionUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_id", nullable = false)
    private Mentoring mentoring;

    @Enumerated(EnumType.STRING)
    private MentoringSessionStatus status;

    @OneToMany(mappedBy = "mentoringSession", cascade = CascadeType.ALL)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private MentoringSession(Reservation reservation) {
        this.sessionUrl = java.util.UUID.randomUUID().toString();
        this.reservation = reservation;
        this.mentoring = reservation.getMentoring();
        this.status = MentoringSessionStatus.CLOSED;
        this.chatMessages = new ArrayList<>();
    }

    public static MentoringSession create(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new IllegalArgumentException("Reservation must be APPROVED to create a MentoringSession.");
        }
        return MentoringSession.builder()
                .reservation(reservation)
                .build();
    }

    private MentoringSession updateStatus(MentoringSessionStatus status) {
        this.status = status;
        return this;
    }

    public MentoringSession openSession(Mentor mentor) {
        if (!mentoring.isOwner(mentor)) {
            throw new IllegalArgumentException("Only the mentor who owns the mentoring can open the session.");
        }
        return updateStatus(MentoringSessionStatus.OPEN);
    }

    public MentoringSession closeSession(Mentor mentor) {
        if (!mentoring.isOwner(mentor)) {
            throw new IllegalArgumentException("Only the mentor who owns the mentoring can open the session.");
        }
        return updateStatus(MentoringSessionStatus.CLOSED);
    }
}
