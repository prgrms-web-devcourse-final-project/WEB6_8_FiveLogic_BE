package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentoringRepository extends JpaRepository<Mentoring, Long>, MentoringRepositoryCustom {
    List<Mentoring> findByMentorId(Long mentorId);
    Optional<Mentoring> findTopByOrderByIdDesc();
    boolean existsByMentorIdAndTitle(Long mentorId, String title);
    boolean existsByMentorIdAndTitleAndIdNot(Long mentorId, String title, Long MentoringId);
}
