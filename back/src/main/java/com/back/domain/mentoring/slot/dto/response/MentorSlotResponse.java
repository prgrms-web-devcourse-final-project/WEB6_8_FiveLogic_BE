package com.back.domain.mentoring.slot.dto.response;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MentorSlotResponse(
    @Schema(description = "멘토 슬롯 ID")
    Long mentorSlotId,

    @Schema(description = "멘토 ID")
    Long mentorId,

    @Schema(description = "멘토링 ID")
    Long mentoringId,

    @Schema(description = "멘토링 제목")
    String mentoringTitle,

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
    public static MentorSlotResponse from(MentorSlot mentorSlot, Mentoring mentoring) {
        return new MentorSlotResponse(
            mentorSlot.getId(),
            mentorSlot.getMentor().getId(),
            mentoring.getId(),
            mentoring.getTitle(),
            mentorSlot.getStartDateTime(),
            mentorSlot.getEndDateTime(),
            mentorSlot.getStatus(),
            mentorSlot.getCreateDate(),
            mentorSlot.getModifyDate()
        );
    }
}
