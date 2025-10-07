package com.back.domain.roadmap.roadmap.event;

public class MentorRoadmapSaveEvent {
    private final Long jobId;

    public MentorRoadmapSaveEvent(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

}
