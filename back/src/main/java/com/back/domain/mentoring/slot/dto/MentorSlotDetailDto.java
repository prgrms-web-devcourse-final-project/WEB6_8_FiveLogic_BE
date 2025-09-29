package com.back.domain.mentoring.slot.dto;

import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MentorSlotDetailDto(
    @Schema(description = "멘토 슬롯 ID")
    Long mentorSlotId,
    @Schema(description = "시작 일시")
    LocalDateTime startDateTime,
    @Schema(description = "종료 일시")
    LocalDateTime endDateTime,
    @Schema(description = "멘토 슬롯 상태")
    MentorSlotStatus mentorSlotStatus,
    @Schema(description = "생성일")
    LocalDateTime createDate,
    @Schema(description = "수정일")
    LocalDateTime modifyDate
) {
    public static MentorSlotDetailDto from(MentorSlot mentorSlot) {
        return new MentorSlotDetailDto(
            mentorSlot.getId(),
            mentorSlot.getStartDateTime(),
            mentorSlot.getEndDateTime(),
            mentorSlot.getStatus(),
            mentorSlot.getCreateDate(),
            mentorSlot.getModifyDate()
        );
    }
}
