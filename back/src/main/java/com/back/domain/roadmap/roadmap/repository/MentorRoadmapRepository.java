package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentorRoadmapRepository extends JpaRepository<MentorRoadmap, Long> {

     @Query("""
         SELECT mr FROM MentorRoadmap mr
         LEFT JOIN FETCH mr.nodes n
         WHERE mr.id = :id
         ORDER BY n.stepOrder ASC
         """)
     Optional<MentorRoadmap> findByIdWithNodes(@Param("id") Long id);

    @Query("""
        SELECT mr FROM MentorRoadmap mr
        LEFT JOIN FETCH mr.nodes n
        WHERE mr.mentorId = :mentorId
        ORDER BY n.stepOrder ASC
        """)
    Optional<MentorRoadmap> findByMentorIdWithNodes(@Param("mentorId") Long mentorId);

    // 기본 정보만 조회하는 메서드
    Optional<MentorRoadmap> findByMentorId(Long mentorId);

    boolean existsByMentorId(Long mentorId);
}