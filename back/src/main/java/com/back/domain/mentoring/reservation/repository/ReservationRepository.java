package com.back.domain.mentoring.reservation.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findTopByOrderByIdDesc();
    Optional<Reservation> findByMentorSlotIdAndStatusIn(Long mentorSlotId, List<ReservationStatus> statuses);

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.id = :reservationId
        AND (r.mentee.member = :member
            OR r.mentor.member = :member)
        """)
    Optional<Reservation> findByIdAndMember(
        @Param("reservationId") Long reservationId,
        @Param("member") Member member
    );

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.mentor.member = :member
        ORDER BY r.mentorSlot.startDateTime DESC
        """)
    Page<Reservation> findAllByMentorMember(
        @Param("member") Member member,
        Pageable pageable
    );

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.mentee.member = :member
        ORDER BY r.mentorSlot.startDateTime DESC
        """)
    Page<Reservation> findAllByMenteeMember(
        @Param("member") Member member,
        Pageable pageable
    );

    boolean existsByMentoringId(Long mentoringId);

    /**
     * 예약 기록 존재 여부 확인 (모든 상태 포함)
     * - 슬롯 삭제 시 데이터 무결성 검증용
     * - 취소/거절된 예약도 히스토리로 보존
     */
    boolean existsByMentorSlotId(Long slotId);
}
