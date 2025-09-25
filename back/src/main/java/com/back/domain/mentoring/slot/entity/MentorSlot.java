package com.back.domain.mentoring.slot.entity;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorSlotStatus status;

    @Builder
    public MentorSlot(Mentor mentor, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.mentor = mentor;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = MentorSlotStatus.AVAILABLE;
    }

    // =========================
    // TODO - 현재 상태
    // 1. reservation 필드에는 활성 예약(PENDING, APPROVED)만 세팅
    // 2. 취소/거절 예약은 DB에 남기고 reservation 필드에는 연결하지 않음
    // 3. 슬롯 재생성 불필요, 상태 기반 isAvailable() 로 새 예약 가능 판단
    //
    // TODO - 추후 변경
    // 1. 1:N 구조로 리팩토링
    //    - MentorSlot에 여러 Reservation 연결 가능
    //    - 모든 예약 기록(히스토리) 보존
    // 2. 상태 기반 필터링 유지: 활성 예약만 계산 시 사용
    // 3. 이벤트 소싱/분석 등 확장 가능하도록 구조 개선
    // =========================

    public void updateStatus() {
        if (reservation == null) {
            this.status =  MentorSlotStatus.AVAILABLE;
        } else {
            this.status = switch (reservation.getStatus()) {
                case PENDING -> MentorSlotStatus.PENDING;
                case APPROVED -> MentorSlotStatus.APPROVED;
                case COMPLETED -> MentorSlotStatus.COMPLETED;
                case REJECTED, CANCELED -> MentorSlotStatus.AVAILABLE;
            };
        }
    }

    public boolean isAvailable() {
        return reservation == null ||
            reservation.getStatus().equals(ReservationStatus.REJECTED) ||
            reservation.getStatus().equals(ReservationStatus.CANCELED);
    }

    public void update(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
