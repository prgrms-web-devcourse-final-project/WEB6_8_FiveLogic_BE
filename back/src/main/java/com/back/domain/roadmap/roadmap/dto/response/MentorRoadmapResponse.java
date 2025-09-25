package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class MentorRoadmapResponse {
    private final Long id;
    private final Long mentorId;
    private final String title;
    private final String description;
    private final List<RoadmapNodeResponse> nodes;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;

    public MentorRoadmapResponse(MentorRoadmap mentorRoadmap, List<RoadmapNode> nodes) {
        this.id = mentorRoadmap.getId();
        this.mentorId = mentorRoadmap.getMentorId();
        this.title = mentorRoadmap.getTitle();
        this.description = mentorRoadmap.getDescription();
        this.nodes = nodes.stream()
                .map(RoadmapNodeResponse::new)
                .toList();
        this.createdDate = mentorRoadmap.getCreateDate();
        this.modifiedDate = mentorRoadmap.getModifyDate();
    }
}