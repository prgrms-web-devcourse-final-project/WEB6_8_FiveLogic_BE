package com.back.domain.member.mentee.repository;

import com.back.domain.member.mentee.entity.Mentee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MenteeRepository extends JpaRepository<Mentee, Long> {
    // 활성 멘티만 조회 (소프트 삭제된 멘티 제외)
    @Query("SELECT m FROM Mentee m WHERE m.member.id = :memberId AND m.isDeleted = false")
    Optional<Mentee> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT m FROM Mentee m WHERE m.id = :id AND m.isDeleted = false")
    Optional<Mentee> findById(@Param("id") Long id);

    // 삭제된 멘티 포함 조회 (관리자용)
    @Query("SELECT m FROM Mentee m WHERE m.member.id = :memberId")
    Optional<Mentee> findByMemberIdIncludingDeleted(@Param("memberId") Long memberId);
}