package com.back.domain.roadmap.roadmap.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멘토 로드맵 다건 조회 응답")
public record MentorRoadmapListResponse(
        @Schema(description = "로드맵 ID")
        Long id,

        @Schema(description = "로드맵 제목")
        String title,

        @Schema(description = "로드맵 설명 (150자 제한)")
        String description,

        @Schema(description = "멘토 ID")
        Long mentorId,

        @Schema(description = "회원 ID")
        Long memberId,

        @Schema(description = "멘토 닉네임")
        String mentorNickname
) {
    // 정적 팩토리 메서드
    public static MentorRoadmapListResponse of(
            Long id,
            String title,
            String description,
            Long mentorId,
            Long memberId,
            String mentorNickname
    ) {
        return new MentorRoadmapListResponse(
                id,
                title,
                truncateDescription(description),
                mentorId,
                memberId,
                mentorNickname
        );
    }

    // description 자르기 로직 (150자 초과 시 "..." 추가)
    private static String truncateDescription(String description) {
        if (description == null) return null;
        return description.length() > 150 ? description.substring(0, 150) + "..." : description;
    }
}