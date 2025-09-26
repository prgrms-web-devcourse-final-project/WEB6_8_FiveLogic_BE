package com.back.domain.member.mentor.repository;

import com.back.domain.member.mentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByMemberId(Long memberId);
}