package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.job.job.entity.Job;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRoadmapRepository extends JpaRepository<JobRoadmap, Long> {

    @Query("SELECT jr FROM JobRoadmap jr JOIN FETCH jr.job")
    List<JobRoadmap> findAllWithJob();

    @Query("""
            SELECT jr FROM JobRoadmap jr
            JOIN FETCH jr.job j
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<JobRoadmap> findAllWithJobAndKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT jr FROM JobRoadmap jr
            JOIN FETCH jr.job
            LEFT JOIN FETCH jr.nodes n
            LEFT JOIN FETCH n.task t
            WHERE jr.id = :id
            ORDER BY n.level, n.stepOrder""")
    Optional<JobRoadmap> findByIdWithJobAndNodes(@Param("id") Long id);

    Optional<JobRoadmap> findByJob(Job job);
}