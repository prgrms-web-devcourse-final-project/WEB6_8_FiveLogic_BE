package com.back.domain.member.mentee.repository;

import com.back.domain.member.mentee.entity.Mentee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenteeRepository extends JpaRepository<Mentee, Long> {
    Optional<Mentee> findByMemberId(Long memberId);
}