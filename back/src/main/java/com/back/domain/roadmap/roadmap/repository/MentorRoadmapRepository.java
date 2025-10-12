package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentorRoadmapRepository extends JpaRepository<MentorRoadmap, Long> {

    /**
     * 로드맵 ID로 상세 조회 (노드 포함)
     * 로드맵 ID 기반 상세 조회 API용
     */
    @Query("""
         SELECT mr FROM MentorRoadmap mr
         LEFT JOIN FETCH mr.nodes n
         WHERE mr.id = :id
         """)
    Optional<MentorRoadmap> findByIdWithNodes(@Param("id") Long id);

    /**
     * 멘토 ID로 상세 조회 (노드 포함)
     * - 멘토 ID 기반 상세 조회 API용
     * - 멘토는 이미 WHERE 조건에서 활용되므로 별도 fetch 하지 않음
     */
    @Query("""
        SELECT mr FROM MentorRoadmap mr
        LEFT JOIN FETCH mr.nodes n
        WHERE mr.mentor.id = :mentorId
        """)
    Optional<MentorRoadmap> findByMentorIdWithNodes(@Param("mentorId") Long mentorId);

    /**
     * 멘토의 로드맵 존재 여부 확인
     * - 로드맵 생성 시 중복 체크용
     */
    boolean existsByMentor(Mentor mentor);

    /**
     * 직업 ID로 멘토 로드맵 목록 조회 (노드 포함)
     * - 특정 직업에 속한 멘토들의 로드맵 목록 조회
     * - 통합된 직업 로드맵 생성용
     */
    @Query("""
    SELECT mr FROM MentorRoadmap mr
    LEFT JOIN FETCH mr.nodes n
    WHERE mr.mentor.job.id = :jobId
    ORDER BY mr.id, n.stepOrder
    """)
    List<MentorRoadmap> findAllByMentorJobIdWithNodes(@Param("jobId") Long jobId);
}