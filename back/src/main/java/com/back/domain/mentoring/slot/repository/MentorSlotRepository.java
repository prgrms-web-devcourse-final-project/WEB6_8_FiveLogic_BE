package com.back.domain.mentoring.slot.repository;

import com.back.domain.mentoring.slot.dto.response.MentorSlotDto;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MentorSlotRepository extends JpaRepository<MentorSlot, Long> {
    Optional<MentorSlot> findTopByOrderByIdDesc();

    boolean existsByMentorId(Long mentorId);
    long countByMentorId(Long mentorId);
    void deleteAllByMentorId(Long mentorId);

    @Query("""
        SELECT new com.back.domain.mentoring.slot.dto.response.MentorSlotDto(
            ms.id,
            ms.mentor.id,
            ms.mentor.member.id,
            ms.startDateTime,
            ms.endDateTime,
            ms.status,
            r.id
        )
        FROM MentorSlot ms
        LEFT JOIN Reservation r
            ON ms.id = r.mentorSlot.id
            AND r.status IN ('PENDING', 'APPROVED', 'COMPLETED')
        WHERE ms.mentor.id = :mentorId
        AND ms.startDateTime < :end
        AND ms.endDateTime >= :start
        ORDER BY ms.startDateTime ASC
        """)
    List<MentorSlotDto> findMySlots(
        @Param("mentorId") Long mentorId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT new com.back.domain.mentoring.slot.dto.response.MentorSlotDto(
                ms.id,
                ms.mentor.id,
                ms.mentor.member.id,
                ms.startDateTime,
                ms.endDateTime,
                ms.status,
                NULL
        )
        FROM MentorSlot ms
        WHERE ms.mentor.id = :mentorId
        AND ms.status = 'AVAILABLE'
        AND ms.startDateTime >= CURRENT_TIMESTAMP
        AND ms.startDateTime < :end
        AND ms.endDateTime >= :start
        ORDER BY ms.startDateTime ASC
        """)
    List<MentorSlotDto> findAvailableSlots(
        @Param("mentorId") Long mentorId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

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

    @Modifying
    @Query("""
        UPDATE MentorSlot  ms
        SET ms.status = com.back.domain.mentoring.slot.constant.MentorSlotStatus.EXPIRED
        WHERE ms.startDateTime < :now
        AND ms.status = com.back.domain.mentoring.slot.constant.MentorSlotStatus.AVAILABLE
        """)
    int expirePassedSlots(
        @Param("now") LocalDateTime now
    );
}
