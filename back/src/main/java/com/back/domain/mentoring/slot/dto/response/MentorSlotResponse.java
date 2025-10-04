package com.back.domain.mentoring.slot.dto.response;

import com.back.domain.member.mentor.dto.MentorDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDto;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.slot.dto.MentorSlotDetailDto;
import com.back.domain.mentoring.slot.entity.MentorSlot;

import java.util.List;

public record MentorSlotResponse(
    MentorSlotDetailDto mentorSlot,
    MentorDto mentor,
    List<MentoringDto> mentorings
) {
    public static MentorSlotResponse from(MentorSlot mentorSlot, List<Mentoring> mentorings) {
        return new MentorSlotResponse(
            MentorSlotDetailDto.from(mentorSlot),
            MentorDto.from(mentorSlot.getMentor()),
            mentorings.stream()
                .map(MentoringDto::from)
                .toList()
        );
    }
}
