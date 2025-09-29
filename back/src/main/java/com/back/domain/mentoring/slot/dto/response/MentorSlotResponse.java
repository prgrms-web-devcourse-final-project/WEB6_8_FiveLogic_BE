package com.back.domain.mentoring.slot.dto.response;

import com.back.domain.member.mentor.dto.MentorDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDto;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.slot.dto.MentorSlotDetailDto;
import com.back.domain.mentoring.slot.entity.MentorSlot;

public record MentorSlotResponse(
    MentorSlotDetailDto mentorSlot,
    MentorDto mentor,
    MentoringDto mentoring
) {
    public static MentorSlotResponse from(MentorSlot mentorSlot, Mentoring mentoring) {
        return new MentorSlotResponse(
            MentorSlotDetailDto.from(mentorSlot),
            MentorDto.from(mentorSlot.getMentor()),
            MentoringDto.from(mentoring)
        );
    }
}
