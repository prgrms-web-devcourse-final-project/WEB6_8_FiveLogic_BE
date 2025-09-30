package com.back.domain.mentoring.reservation.constant;

public enum ReservationStatus {
    PENDING,    // 예약 승인 대기
    APPROVED,   // 승인됨
    REJECTED,   // 거절됨
    CANCELED,   // 취소됨
    COMPLETED;   // 완료됨

    public boolean canApprove() {
        return this == PENDING;
    }

    public boolean canReject() {
        return this == PENDING;
    }

    public boolean canCancel() {
        return this == PENDING || this == APPROVED;
    }

    public boolean canComplete() {
        return this == APPROVED;
    }
}
