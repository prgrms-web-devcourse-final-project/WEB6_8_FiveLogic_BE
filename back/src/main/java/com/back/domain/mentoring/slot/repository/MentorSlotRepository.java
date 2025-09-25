package com.back.domain.mentoring.slot.repository;

import com.back.domain.mentoring.slot.entity.MentorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MentorSlotRepository extends JpaRepository<MentorSlot, Long> {
    Optional<MentorSlot> findTopByOrderByIdDesc();
    boolean existsByMentorId(Long mentorId);
    void deleteAllByMentorId(Long mentorId);

    // TODO: 현재는 시간 겹침만 체크, 추후 1:N 구조 시 활성 예약 기준으로 변경
    @Query("""
        SELECT CASE WHEN COUNT(ms) > 0
                THEN TRUE
                ELSE FALSE END
        FROM MentorSlot ms
        WHERE ms.mentor.id = :mentorId
        AND (ms.startDateTime < :end AND ms.endDateTime > :start)
        """)
    boolean existsOverlappingSlot(
        @Param("mentorId") Long mentorId,
        @Param("start")LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT CASE WHEN COUNT(ms) > 0
                THEN TRUE
                ELSE FALSE END
        FROM MentorSlot ms
        WHERE ms.mentor.id = :mentorId
        AND ms.id != :slotId
        AND (ms.startDateTime < :end AND ms.endDateTime > :start)
        """)
    boolean existsOverlappingExcept(
        @Param("mentorId") Long mentorId,
        @Param("slotId") Long slotId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    long countByMentorId(Long id);
}
