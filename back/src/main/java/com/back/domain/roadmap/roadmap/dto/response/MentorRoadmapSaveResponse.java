package com.back.domain.roadmap.roadmap.dto.response;

import java.time.LocalDateTime;

// 순수 데이터 전송 객체 - 엔티티에 의존하지 않음
public record MentorRoadmapSaveResponse(
    Long id,
    Long mentorId,
    String title,
    String description,
    int nodeCount,
    LocalDateTime createDate
) {}