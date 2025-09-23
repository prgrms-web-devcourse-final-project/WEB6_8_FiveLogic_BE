package com.back.domain.mentoring.slot.repository;

import com.back.domain.mentoring.slot.entity.MentorSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorSlotRepository extends JpaRepository<MentorSlot, Long> {
    boolean existsByMentorId(Long mentorId);

    void deleteAllByMentorId(Long mentorId);
}
