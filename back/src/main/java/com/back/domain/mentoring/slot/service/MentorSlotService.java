package com.back.domain.mentoring.slot.service;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRepetitionRequest;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotDto;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorSlotService {

    private final MentorSlotRepository mentorSlotRepository;
    private final MentoringStorage mentorStorage;

    @Transactional(readOnly = true)
    public List<MentorSlotDto> getMyMentorSlots(Mentor mentor, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeValidator.validateTime(startDate, endDate);

        return mentorSlotRepository.findMySlots(mentor.getId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<MentorSlotDto> getAvailableMentorSlots(Long mentorId, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeValidator.validateTime(startDate, endDate);

        return mentorSlotRepository.findAvailableSlots(mentorId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public MentorSlotResponse getMentorSlot(Long slotId) {
        MentorSlot mentorSlot = mentorStorage.findMentorSlot(slotId);
        List<Mentoring> mentorings = mentorStorage.findMentoringsByMentorId(mentorSlot.getMentor().getId());

        return MentorSlotResponse.from(mentorSlot, mentorings);
    }

    @Transactional
    public MentorSlotResponse createMentorSlot(MentorSlotRequest reqDto, Mentor mentor) {
        List<Mentoring> mentorings = mentorStorage.findMentoringsByMentorId(mentor.getId());

        DateTimeValidator.validateTimeSlot(reqDto.startDateTime(), reqDto.endDateTime());
        validateOverlappingSlots(mentor, reqDto.startDateTime(), reqDto.endDateTime());

        MentorSlot mentorSlot = MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(reqDto.startDateTime())
            .endDateTime(reqDto.endDateTime())
            .build();
        mentorSlotRepository.save(mentorSlot);

        return MentorSlotResponse.from(mentorSlot, mentorings);
    }

    @Transactional
    public void createMentorSlotRepetition(MentorSlotRepetitionRequest reqDto, Mentor mentor) {
        List<MentorSlot> mentorSlots = new ArrayList<>();

        DateTimeValidator.validateRepetitionSlot(reqDto.repeatStartDate(), reqDto.startTime(),
            reqDto.repeatEndDate(), reqDto.endTime());

        // 지정한 요일별로 슬롯 목록 생성
        for(DayOfWeek targetDayOfWeek : reqDto.daysOfWeek()) {
            mentorSlots.addAll(generateSlotsForDayOfWeek(reqDto, targetDayOfWeek, mentor));
        }
        mentorSlotRepository.saveAll(mentorSlots);
    }

    @Transactional
    public MentorSlotResponse updateMentorSlot(Long slotId, MentorSlotRequest reqDto, Mentor mentor) {
        MentorSlot mentorSlot = mentorStorage.findMentorSlot(slotId);

        validateOwner(mentorSlot, mentor);
        // 활성화된 예약이 있으면 수정 불가
        validateModification(mentorSlot);

        DateTimeValidator.validateTimeSlot(reqDto.startDateTime(), reqDto.endDateTime());
        validateOverlappingExcept(mentor, mentorSlot, reqDto.startDateTime(), reqDto.endDateTime());

        mentorSlot.updateTime(reqDto.startDateTime(), reqDto.endDateTime());

        List<Mentoring> mentorings = mentorStorage.findMentoringsByMentorId(mentor.getId());
        return MentorSlotResponse.from(mentorSlot, mentorings);
    }

    @Transactional
    public void deleteMentorSlot(Long slotId, Mentor mentor) {
        MentorSlot mentorSlot = mentorStorage.findMentorSlot(slotId);

        validateOwner(mentorSlot, mentor);
        // 예약 기록 존재 여부 검증 (모든 예약 기록 확인)
        validateNoReservationHistory(slotId);

        mentorSlotRepository.delete(mentorSlot);
    }


    // ===== 반복 슬롯 생성 로직 =====

    /**
     * 특정 요일에 해당하는 모든 슬롯들을 생성
     */
    private List<MentorSlot> generateSlotsForDayOfWeek(MentorSlotRepetitionRequest reqDto, DayOfWeek targetDayOfWeek, Mentor mentor) {
        List<MentorSlot> mentorSlots = new ArrayList<>();
        LocalDate currentDate = findNextOrSameDayOfWeek(reqDto.repeatStartDate(), targetDayOfWeek);

        // 해당 요일에 대해 주 단위로 반복하여 슬롯 생성
        while (!currentDate.isAfter(reqDto.repeatEndDate())) {
            LocalDateTime startDateTime = LocalDateTime.of(currentDate, reqDto.startTime());
            LocalDateTime endDateTime = LocalDateTime.of(currentDate, reqDto.endTime());

            validateOverlappingSlots(mentor, startDateTime, endDateTime);

            MentorSlot mentorSlot = MentorSlot.builder()
                .mentor(mentor)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .build();
            mentorSlots.add(mentorSlot);

            currentDate = currentDate.plusWeeks(1);
        }
        return mentorSlots;
    }

    /**
     * 시작일부터 해당 요일의 첫 번째 날짜를 찾기
     */
    private LocalDate findNextOrSameDayOfWeek(LocalDate startDate, DayOfWeek targetDay) {
        return startDate.with(TemporalAdjusters.nextOrSame(targetDay));
    }


    // ===== 검증 메서드 =====

    private static void validateOwner(MentorSlot mentorSlot, Mentor mentor) {
        if (!mentorSlot.isOwnerBy(mentor)) {
            throw new ServiceException(MentorSlotErrorCode.NOT_OWNER);
        }
    }

    /**
     * 주어진 시간대가 기존 슬롯과 겹치는지 검증
     * - mentor의 기존 모든 슬롯과 비교
     */
    private void validateOverlappingSlots(Mentor mentor, LocalDateTime start, LocalDateTime end) {
        if (mentorSlotRepository.existsOverlappingSlot(mentor.getId(), start, end)) {
            throw new ServiceException(MentorSlotErrorCode.OVERLAPPING_SLOT);
        }
    }

    /**
     * 특정 슬롯을 제외하고 시간대가 겹치는지 검증
     * - 대상 슬롯(self)은 제외
     */
    private void validateOverlappingExcept(Mentor mentor, MentorSlot mentorSlot, LocalDateTime start, LocalDateTime end) {
        if (mentorSlotRepository.existsOverlappingExcept(mentor.getId(), mentorSlot.getId(), start, end)) {
            throw new ServiceException(MentorSlotErrorCode.OVERLAPPING_SLOT);
        }
    }

    /**
     * 활성화된 예약이 있으면 수정 불가
     * - 예약 취소, 예약 거절 상태는 수정 가능
     */
    private static void validateModification(MentorSlot mentorSlot) {
        if (!mentorSlot.isAvailable()) {
            throw new ServiceException(MentorSlotErrorCode.CANNOT_UPDATE_RESERVED_SLOT);
        }
    }

    /**
     * 예약 기록이 하나라도 있으면 삭제 불가
     * - 데이터 무결성 보장
     * - 히스토리 보존
     */
    private void validateNoReservationHistory(Long slotId) {
        if (mentorStorage.hasReservationForMentorSlot(slotId)) {
            throw new ServiceException(MentorSlotErrorCode.CANNOT_DELETE_RESERVED_SLOT);
        }
    }
}
