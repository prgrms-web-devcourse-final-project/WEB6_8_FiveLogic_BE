package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringRepository extends JpaRepository<Mentoring, Long> {
}
