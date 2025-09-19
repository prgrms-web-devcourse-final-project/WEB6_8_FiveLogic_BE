package com.back.domain.mentoring.slot.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class MentorSlot extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @OneToOne(mappedBy = "mentorSlot")
    private Reservation reservation;

    public MentorSlotStatus getStatus() {
        if (reservation == null) {
            return MentorSlotStatus.AVAILABLE;
        }

        return switch (reservation.getStatus()) {
            case PENDING -> MentorSlotStatus.PENDING;
            case APPROVED -> MentorSlotStatus.APPROVED;
            case COMPLETED -> MentorSlotStatus.COMPLETED;
            default -> MentorSlotStatus.AVAILABLE;
        };
    }

    public boolean isAvailable() {
        return reservation == null ||
            reservation.getStatus().equals(ReservationStatus.REJECTED) ||
            reservation.getStatus().equals(ReservationStatus.CANCELED);
    }
}
