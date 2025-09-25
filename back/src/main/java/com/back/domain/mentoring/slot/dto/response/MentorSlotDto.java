package com.back.domain.mentoring.slot.dto.response;

import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MentorSlotDto(
    @Schema(description = "멘토 슬롯 ID")
    Long mentorSlotId,
    @Schema(description = "멘토 ID")
    Long mentorId,
    @Schema(description = "시작 일시")
    LocalDateTime startDateTime,
    @Schema(description = "종료 일시")
    LocalDateTime endDateTime,
    @Schema(description = "멘토 슬롯 상태")
    MentorSlotStatus mentorSlotStatus
) {
    public static MentorSlotDto from(MentorSlot mentorSlot) {
        return new MentorSlotDto(
            mentorSlot.getId(),
            mentorSlot.getMentor().getId(),
            mentorSlot.getStartDateTime(),
            mentorSlot.getEndDateTime(),
            mentorSlot.getStatus()
        );
    }
}
