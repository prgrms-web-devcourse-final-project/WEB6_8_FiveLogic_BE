package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapIntegrationQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobRoadmapIntegrationQueueRepository extends JpaRepository<JobRoadmapIntegrationQueue, Long> {
    @Query("SELECT q FROM JobRoadmapIntegrationQueue q ORDER BY q.requestedAt ASC")
    List<JobRoadmapIntegrationQueue> findAllOrderByRequestedAt();
}
