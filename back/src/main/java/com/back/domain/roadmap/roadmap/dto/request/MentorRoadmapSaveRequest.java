package com.back.domain.roadmap.roadmap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MentorRoadmapSaveRequest(
        @NotBlank(message = "로드맵 제목은 필수입니다.")
        @Size(max = 100, message = "로드맵 제목은 100자를 초과할 수 없습니다.")
        String title,

        @Size(max = 1000, message = "로드맵 설명은 1000자를 초과할 수 없습니다.")
        String description,

        @NotEmpty(message = "로드맵 노드는 최소 1개 이상 필요합니다.")
        @Valid
        List<RoadmapNodeRequest> nodes
) {}