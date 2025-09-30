package com.back.domain.mentoring.session.entity;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
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

    // 화면 공유, WebRTC 관련 필드 등 추가 가능

    @Builder(access = AccessLevel.PRIVATE)
    private MentoringSession(Reservation reservation) {
        this.sessionUrl = java.util.UUID.randomUUID().toString();
        this.reservation = reservation;
        this.mentoring = reservation.getMentoring();
        this.status = MentoringSessionStatus.CLOSED;
        this.chatMessages = new ArrayList<>();
    }

    public static MentoringSession create(Reservation reservation) {
        return MentoringSession.builder()
                .reservation(reservation)
                .build();
    }

    private MentoringSession updateStatus(MentoringSessionStatus status) {
        this.status = status;
        return this;
    }

    public MentoringSession openSession() {
        return updateStatus(MentoringSessionStatus.OPEN);
    }

    public MentoringSession closeSession() {
        return updateStatus(MentoringSessionStatus.CLOSED);
    }
}
