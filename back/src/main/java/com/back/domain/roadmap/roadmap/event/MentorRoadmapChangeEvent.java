package com.back.domain.roadmap.roadmap.event;

public class MentorRoadmapChangeEvent {
    private final Long jobId;

    public MentorRoadmapChangeEvent(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }
}
