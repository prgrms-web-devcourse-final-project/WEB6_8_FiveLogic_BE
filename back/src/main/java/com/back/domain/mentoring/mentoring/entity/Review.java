package com.back.domain.mentoring.mentoring.entity;

import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private double rating;

    @Column(columnDefinition = "TEXT")
    private String content;
}
