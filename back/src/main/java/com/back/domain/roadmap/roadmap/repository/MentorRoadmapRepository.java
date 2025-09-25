package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MentorRoadmapRepository extends JpaRepository<MentorRoadmap, Long> {

    @Query("""
        SELECT mr FROM MentorRoadmap mr
        JOIN FETCH mr.rootNode rn
        WHERE mr.id = :id
        """)
    Optional<MentorRoadmap> findByIdWithRootNode(@Param("id") Long id);

    @Query("""
        SELECT mr FROM MentorRoadmap mr
        WHERE mr.mentorId = :mentorId
        ORDER BY mr.createDate DESC
        """)
    Optional<MentorRoadmap> findByMentorId(@Param("mentorId") Long mentorId);

    boolean existsByMentorId(Long mentorId);
}