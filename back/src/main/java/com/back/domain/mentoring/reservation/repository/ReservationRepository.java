package com.back.domain.mentoring.reservation.repository;

import com.back.domain.mentoring.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findTopByOrderByIdDesc();

    boolean existsByMentoringId(Long mentoringId);

    /**
     * 예약 기록 존재 여부 확인 (모든 상태 포함)
     * - 슬롯 삭제 시 데이터 무결성 검증용
     * - 취소/거절된 예약도 히스토리로 보존
     */
    boolean existsByMentorSlotId(Long slotId);
}
