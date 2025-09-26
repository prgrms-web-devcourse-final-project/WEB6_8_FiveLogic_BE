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
         LEFT JOIN FETCH n.task
         WHERE mr.id = :id
         """)
     Optional<MentorRoadmap> findByIdWithNodes(@Param("id") Long id);

    @Query("""
        SELECT mr FROM MentorRoadmap mr
        LEFT JOIN FETCH mr.nodes n
        LEFT JOIN FETCH n.task
        WHERE mr.mentorId = :mentorId
        """)
    Optional<MentorRoadmap> findByMentorIdWithNodes(@Param("mentorId") Long mentorId);

    // 기본 정보만 조회하는 메서드도 유지
    Optional<MentorRoadmap> findByMentorId(Long mentorId);

    boolean existsByMentorId(Long mentorId);
}