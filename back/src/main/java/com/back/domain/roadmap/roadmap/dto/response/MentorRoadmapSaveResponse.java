package com.back.domain.roadmap.roadmap.dto.response;

import java.time.LocalDateTime;

public record MentorRoadmapSaveResponse(
    Long id,
    Long mentorId,
    String title,
    String description,
    int nodeCount,
    LocalDateTime createDate
) {}