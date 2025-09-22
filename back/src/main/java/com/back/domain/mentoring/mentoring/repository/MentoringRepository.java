package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {
    Optional<Mentoring> findTopByOrderByIdDesc();

    boolean existsByMentorId(Long mentorId);
}
