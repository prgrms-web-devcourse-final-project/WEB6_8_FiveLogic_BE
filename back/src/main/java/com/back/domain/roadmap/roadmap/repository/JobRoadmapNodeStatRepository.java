package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRoadmapNodeStatRepository extends JpaRepository<JobRoadmapNodeStat, Long> {

    @Query("""
            SELECT s FROM JobRoadmapNodeStat s
            JOIN FETCH s.node n
            WHERE n.roadmapId = :roadmapId
            """)
    List<JobRoadmapNodeStat> findByNode_RoadmapIdWithNode(@Param("roadmapId") Long roadmapId);
}
