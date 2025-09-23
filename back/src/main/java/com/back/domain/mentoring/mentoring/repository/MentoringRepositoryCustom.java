package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MentoringRepositoryCustom {
    Page<Mentoring> searchMentorings(String keyword, Pageable pageable);
}
