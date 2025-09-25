package com.back.domain.mentoring.slot.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MentorSlotRepetitionRequest(
    @Schema(description = "반복 시작일")
    @NotNull
    LocalDate repeatStartDate,

    @Schema(description = "반복 종료일")
    @NotNull
    LocalDate repeatEndDate,

    @Schema(description = "반복 요일")
    @NotEmpty
    List<DayOfWeek> daysOfWeek,

    @Schema(description = "시작 시간")
    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime,

    @Schema(description = "종료 시간")
    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime
){
}
