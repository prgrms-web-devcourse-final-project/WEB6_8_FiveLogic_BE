package com.back.domain.mentoring.slot.constant;

public enum MentorSlotStatus {
    AVAILABLE,  // 예약 가능
    PENDING,    // 예약 승인 대기
    APPROVED,   // 예약 승인됨(확정)
    COMPLETED,  // 멘토링 완료
    EXPIRED     // 만료
}
