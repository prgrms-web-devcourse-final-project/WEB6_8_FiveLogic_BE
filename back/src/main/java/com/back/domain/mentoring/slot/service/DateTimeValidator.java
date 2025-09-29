package com.back.domain.mentoring.slot.service;

import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.global.exception.ServiceException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateTimeValidator {

    private static final int MIN_SLOT_DURATION = 20;

    public static void validateNotNull(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new ServiceException(MentorSlotErrorCode.START_TIME_REQUIRED);
        }
        if (end == null) {
            throw new ServiceException(MentorSlotErrorCode.END_TIME_REQUIRED);
        }
    }

    public static void validateEndTimeAfterStart(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ServiceException(MentorSlotErrorCode.END_TIME_BEFORE_START);
        }
    }

    public static void validateStartTimeNotInPast(LocalDateTime start) {
        if (start.isBefore(LocalDateTime.now())) {
            throw new ServiceException(MentorSlotErrorCode.START_TIME_IN_PAST);
        }
    }

    public static void validateMinimumDuration(LocalDateTime start, LocalDateTime end) {
        long duration = Duration.between(start, end).toMinutes();
        if (duration < MIN_SLOT_DURATION) {
            throw new ServiceException(MentorSlotErrorCode.INSUFFICIENT_SLOT_DURATION);
        }
    }

    public static void validateTime(LocalDateTime start, LocalDateTime end) {
        validateNotNull(start, end);
        validateEndTimeAfterStart(start, end);
    }

    public static void validateTimeSlot(LocalDateTime start, LocalDateTime end) {
        validateNotNull(start, end);
        validateEndTimeAfterStart(start, end);

        validateStartTimeNotInPast(start);
        validateMinimumDuration(start, end);
    }

    public static void validateRepetitionSlot(LocalDate startDate, LocalTime startTime,
                                              LocalDate endDate, LocalTime endTime) {
        if (endDate.isBefore(startDate)) {
            throw new ServiceException(MentorSlotErrorCode.END_TIME_BEFORE_START);
        }

        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(startDate, endTime);

        validateTimeSlot(startDateTime, endDateTime);
    }
}
