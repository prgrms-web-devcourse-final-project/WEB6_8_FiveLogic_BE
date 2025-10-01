package com.back.domain.roadmap.roadmap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직업 로드맵 다건 조회 응답")
public record JobRoadmapListResponse(
        @Schema(description = "로드맵 ID")
        Long id,

        @Schema(description = "직업명")
        String jobName,

        @Schema(description = "직업 설명 (150자 제한)")
        String jobDescription
) {
    // 정적 팩토리 메서드
    public static JobRoadmapListResponse of(Long id, String jobName, String jobDescription) {
        return new JobRoadmapListResponse(id, jobName, truncateDescription(jobDescription));
    }

    // description 자르기 로직 (150자 초과 시 "..." 추가)
    private static String truncateDescription(String description) {
        if (description == null) return null;
        return description.length() > 150 ? description.substring(0, 150) + "..." : description;
    }
}