package com.back.domain.mentoring.slot.service;

import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.global.exception.ServiceException;

import java.time.Duration;
import java.time.LocalDateTime;

public class MentorSlotValidator {

    private static final int MIN_SLOT_DURATION = 20;

    public static void validateNotNull(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new ServiceException(MentorSlotErrorCode.START_TIME_REQUIRED);
        }
        if (end == null) {
            throw new ServiceException(MentorSlotErrorCode.END_TIME_REQUIRED);
        }
    }

    public static void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ServiceException(MentorSlotErrorCode.END_TIME_BEFORE_START);
        }
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

    public static void validateTimeSlot(LocalDateTime start, LocalDateTime end) {
        validateNotNull(start, end);
        validateTimeRange(start, end);
        validateMinimumDuration(start, end);
    }
}
