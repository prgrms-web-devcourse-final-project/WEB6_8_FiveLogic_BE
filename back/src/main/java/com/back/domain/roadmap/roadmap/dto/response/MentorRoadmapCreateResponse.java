package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MentorRoadmapCreateResponse {
    private final Long id;
    private final Long mentorId;
    private final String title;
    private final String description;
    private final int nodeCount;
    private final LocalDateTime createdDate;

    public MentorRoadmapCreateResponse(MentorRoadmap mentorRoadmap, int nodeCount) {
        this.id = mentorRoadmap.getId();
        this.mentorId = mentorRoadmap.getMentorId();
        this.title = mentorRoadmap.getTitle();
        this.description = mentorRoadmap.getDescription();
        this.nodeCount = nodeCount;
        this.createdDate = mentorRoadmap.getCreateDate();
    }
}