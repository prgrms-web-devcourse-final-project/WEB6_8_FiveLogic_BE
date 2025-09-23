package com.back.domain.mentoring.mentoring.dto.response;

import com.back.domain.member.mentor.dto.MentorDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDetailDto;

public record MentoringResponse(
    MentoringDetailDto mentoring,
    MentorDto mentor
) {
}
