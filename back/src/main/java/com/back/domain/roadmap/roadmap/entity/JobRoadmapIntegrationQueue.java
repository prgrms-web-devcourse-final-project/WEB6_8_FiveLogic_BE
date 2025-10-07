package com.back.domain.roadmap.roadmap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name= "job_roadmap_integration_queue")
@NoArgsConstructor
public class JobRoadmapIntegrationQueue {
    @Id
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Builder
    public JobRoadmapIntegrationQueue(Long jobId) {
        this.jobId = jobId;
        this.requestedAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    public void updateRequestedAt() {
        this.requestedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount += 1;
    }

    public boolean isMaxRetryExceeded(int maxRetry) {
        return this.retryCount >= maxRetry;
    }

    public Long getJobId() {
        return jobId;
    }
}
