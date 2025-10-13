package com.back.domain.member.mentor.repository;

import com.back.domain.member.mentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    // 활성 멘토만 조회 (소프트 삭제된 멘토 제외)
    @Query("SELECT m FROM Mentor m WHERE m.member.id = :memberId AND m.isDeleted = false")
    Optional<Mentor> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT m FROM Mentor m JOIN FETCH m.member LEFT JOIN FETCH m.job WHERE m.member.id = :memberId AND m.isDeleted = false")
    Optional<Mentor> findByMemberIdWithMember(@Param("memberId") Long memberId);

    @Query("SELECT m FROM Mentor m WHERE m.id = :id AND m.isDeleted = false")
    Optional<Mentor> findById(@Param("id") Long id);

    // 삭제된 멘토 포함 조회 (관리자용)
    @Query("SELECT m FROM Mentor m WHERE m.member.id = :memberId")
    Optional<Mentor> findByMemberIdIncludingDeleted(@Param("memberId") Long memberId);
}