package com.back.domain.mentoring.mentoring.entity;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.mentoring.error.ReviewErrorCode;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "review",
    indexes = {
        @Index(name = "idx_review_mentee", columnList = "mentee_id"),
        @Index(name = "idx_review_reservation", columnList = "reservation_id")
    }
)
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(nullable = false)
    private double rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Review(Reservation reservation, Mentee mentee, double rating, String content) {
        ensureMentee(reservation, mentee);

        this.reservation = reservation;
        this.mentee = mentee;
        this.rating = rating;
        this.content = content;
    }

    public void update(double rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public boolean isMentee(Mentee mentee) {
        return mentee.equals(this.mentee);
    }


    // ===== 헬퍼 메서드 =====

    private void ensureMentee(Reservation reservation, Mentee mentee) {
        if (!reservation.isMentee(mentee)) {
            throw new ServiceException(ReviewErrorCode.FORBIDDEN_NOT_MENTEE);
        }
    }
}
