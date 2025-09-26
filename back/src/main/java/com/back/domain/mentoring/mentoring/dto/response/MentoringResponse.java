package com.back.domain.mentoring.mentoring.dto.response;

import com.back.domain.member.mentor.dto.MentorDetailDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record MentoringResponse(
    @Schema(description = "멘토링")
    MentoringDetailDto mentoring,
    @Schema(description = "멘토")
    MentorDetailDto mentor
) {
}
