package com.back.domain.mentoring.session.repository;

import com.back.domain.mentoring.session.entity.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
}
