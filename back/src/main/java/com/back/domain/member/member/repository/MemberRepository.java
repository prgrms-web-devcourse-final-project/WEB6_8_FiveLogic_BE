package com.back.domain.member.member.repository;

import com.back.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 활성 사용자만 조회 (소프트 삭제된 사용자 제외)
    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.isDeleted = false")
    Optional<Member> findByEmail(@Param("email") String email);

    @Query("SELECT m FROM Member m WHERE m.nickname = :nickname AND m.isDeleted = false")
    Optional<Member> findByNickname(@Param("nickname") String nickname);

    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.isDeleted = false")
    Optional<Member> findById(@Param("id") Long id);

    // 관리자용
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findByEmailIncludingDeleted(@Param("email") String email);

    @Query("SELECT m FROM Member m WHERE m.nickname = :nickname")
    Optional<Member> findByNicknameIncludingDeleted(@Param("nickname") String nickname);

    @Query("SELECT m FROM Member m WHERE m.id = :id")
    Optional<Member> findByIdIncludingDeleted(@Param("id") Long id);

    @Query("SELECT m FROM Member m ORDER BY m.createDate DESC")
    Page<Member> findAllIncludingDeleted(Pageable pageable);
}
