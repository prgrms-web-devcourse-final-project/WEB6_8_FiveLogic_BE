package com.back.domain.mentoring.slot.service;

import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeValidatorTest {

    @Test
    @DisplayName("시작 일시, 종료 일시 기입 시 정상 처리")
    void validateNotNull_success() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        // when & then
        assertDoesNotThrow(() -> DateTimeValidator.validateNotNull(start, end));
    }

    @Test
    @DisplayName("시작 일시 누락 시 예외 발생")
    void validateNotNull_fail_startNull() {
        // given
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
            () -> DateTimeValidator.validateNotNull(null, end));

        assertEquals(MentorSlotErrorCode.START_TIME_REQUIRED.getCode(), exception.getResultCode());
    }

    @Test
    @DisplayName("종료 일시 누락 시 예외 발생")
    void validateNotNull_fail_endNull() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
            () -> DateTimeValidator.validateNotNull(start, null));

        assertEquals(MentorSlotErrorCode.END_TIME_REQUIRED.getCode(), exception.getResultCode());
    }

    @Test
    @DisplayName("종료 일시가 시작 일시보다 이전이면 예외 발생")
    void validateEndTimeAfterStart_fail() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
            () -> DateTimeValidator.validateEndTimeAfterStart(start, end));

        assertEquals(MentorSlotErrorCode.END_TIME_BEFORE_START.getCode(), exception.getResultCode());
    }

    @Test
    @DisplayName("현재 이후의 시작 일시, 종료 일시 기입 시 정상 처리")
    void validateStartTimeNotInPast_success() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        // when & then
        assertDoesNotThrow(() -> DateTimeValidator.validateStartTimeNotInPast(start));
    }

    @Test
    @DisplayName("시작 일시가 현재보다 이전이면 예외 발생")
    void validateStartTimeNotInPast_fail() {
        // given
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
            () -> DateTimeValidator.validateStartTimeNotInPast(start));

        assertEquals(MentorSlotErrorCode.START_TIME_IN_PAST.getCode(), exception.getResultCode());
    }

    @Test
    @DisplayName("20분 이상의 슬롯 기간 입력 시 정상 처리")
    void validateMinimumDuration_success() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        // when & then
        assertDoesNotThrow(() -> DateTimeValidator.validateMinimumDuration(start, end));
    }

    @Test
    @DisplayName("정확히 20분인 경우 정상 처리")
    void validateMinimumDuration_success_exactly20Minutes() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusMinutes(20);

        // when & then
        assertDoesNotThrow(() -> DateTimeValidator.validateMinimumDuration(start, end));
    }

    @Test
    @DisplayName("슬롯 기간이 최소 시간(20분)보다 짧으면 예외 발생")
    void validateMinimumDuration_fail_insufficientSlotDuration() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusMinutes(19);

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
            () -> DateTimeValidator.validateMinimumDuration(start, end));

        assertEquals(MentorSlotErrorCode.INSUFFICIENT_SLOT_DURATION.getCode(), exception.getResultCode());
    }

    @Test
    @DisplayName("모든 검증을 통과하는 정상 케이스")
    void validateTimeSlot_success() {
        // given
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusMinutes(30);

        // when & then
        assertDoesNotThrow(() -> DateTimeValidator.validateTimeSlot(start, end));
    }

    @Test
    @DisplayName("validateTimeSlot 메소드에서 null 체크 예외 발생")
    void validateTimeSlot_fail_nullCheck() {
        // given
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
            () -> DateTimeValidator.validateTimeSlot(null, end));

        assertEquals(MentorSlotErrorCode.START_TIME_REQUIRED.getCode(), exception.getResultCode());
    }
}