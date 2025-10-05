package com.back.domain.mentoring.slot.entity;

import com.back.domain.member.mentor.entity.Mentor;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorSlotStatus status;

    @Version
    private Long version;

    @Builder
    public MentorSlot(Mentor mentor, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.mentor = mentor;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = MentorSlotStatus.AVAILABLE;
    }

    public void updateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public void updateStatus(MentorSlotStatus status) {
        this.status = status;
    }

    /**
     * 새로운 예약이 가능한지 확인
     * - 예약이 없거나
     * - 예약이 취소/거절된 경우 true
     */
    public boolean isAvailable() {
        return status == MentorSlotStatus.AVAILABLE;
    }

    public boolean isOwnerBy(Mentor mentor) {
        return this.mentor.equals(mentor);
    }

    public boolean isPast() {
        return startDateTime.isBefore(LocalDateTime.now());
    }
}
