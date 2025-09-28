package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import java.time.LocalDateTime;
import java.util.List;

public record MentorRoadmapResponse(
    Long id,
    Long mentorId,
    String title,
    String description,
    List<RoadmapNodeResponse> nodes,
    LocalDateTime createdDate,
    LocalDateTime modifiedDate
) {

    // 정적 팩터리 메서드 - MentorRoadmap로부터 Response DTO 생성
    public static MentorRoadmapResponse from(MentorRoadmap mentorRoadmap) {
        List<RoadmapNodeResponse> nodeResponses = mentorRoadmap.getNodes().stream()
                .sorted((n1, n2) -> Integer.compare(n1.getStepOrder(), n2.getStepOrder())) // stepOrder로 정렬
                .map(RoadmapNodeResponse::from)
                .toList();

        return new MentorRoadmapResponse(
            mentorRoadmap.getId(),
            mentorRoadmap.getMentorId(),
            mentorRoadmap.getTitle(),
            mentorRoadmap.getDescription(),
            nodeResponses,
            mentorRoadmap.getCreateDate(),
            mentorRoadmap.getModifyDate()
        );
    }
}