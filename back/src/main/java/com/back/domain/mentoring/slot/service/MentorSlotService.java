package com.back.domain.mentoring.slot.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.dto.MentorSlotDto;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorSlotService {

    private final MentorSlotRepository mentorSlotRepository;
    private final MentorRepository mentorRepository;
    private final MentoringRepository mentoringRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<MentorSlotDto> getMyMentorSlots(Member member, LocalDateTime startDate, LocalDateTime endDate) {
        Mentor mentor = findMentorByMember(member);

        DateTimeValidator.validateTime(startDate, endDate);

        List<MentorSlot> availableSlots = mentorSlotRepository.findMySlots(mentor.getId(), startDate, endDate);

        return availableSlots.stream()
            .map(MentorSlotDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MentorSlotDto> getAvailableMentorSlots(Long mentorId, LocalDateTime startDate, LocalDateTime endDate) {
        validateMentorExists(mentorId);
        DateTimeValidator.validateTime(startDate, endDate);

        List<MentorSlot> availableSlots = mentorSlotRepository.findAvailableSlots(mentorId, startDate, endDate);

        return availableSlots.stream()
            .map(MentorSlotDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public MentorSlotResponse getMentorSlot(Long slotId) {
        MentorSlot mentorSlot = findMentorSlot(slotId);
        Mentoring mentoring = findMentoring(mentorSlot.getMentor());

        return MentorSlotResponse.from(mentorSlot, mentoring);
    }

    @Transactional
    public MentorSlotResponse createMentorSlot(MentorSlotRequest reqDto, Member member) {
        Mentor mentor = findMentorByMember(member);
        Mentoring mentoring = findMentoring(mentor);

        DateTimeValidator.validateTimeSlot(reqDto.startDateTime(), reqDto.endDateTime());
        validateOverlappingSlots(mentor, reqDto.startDateTime(), reqDto.endDateTime());

        MentorSlot mentorSlot = MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(reqDto.startDateTime())
            .endDateTime(reqDto.endDateTime())
            .build();
        mentorSlotRepository.save(mentorSlot);

        return MentorSlotResponse.from(mentorSlot, mentoring);
    }

    @Transactional
    public MentorSlotResponse updateMentorSlot(Long slotId, MentorSlotRequest reqDto, Member member) {
        Mentor mentor = findMentorByMember(member);
        Mentoring mentoring = findMentoring(mentor);
        MentorSlot mentorSlot = findMentorSlot(slotId);

        validateOwner(mentorSlot, mentor);
        // 활성화된 예약이 있으면 수정 불가
        validateModification(mentorSlot);

        DateTimeValidator.validateTimeSlot(reqDto.startDateTime(), reqDto.endDateTime());
        validateOverlappingExcept(mentor, mentorSlot, reqDto.startDateTime(), reqDto.endDateTime());

        mentorSlot.updateTime(reqDto.startDateTime(), reqDto.endDateTime());

        return MentorSlotResponse.from(mentorSlot, mentoring);
    }

    @Transactional
    public void deleteMentorSlot(Long slotId, Member member) {
        Mentor mentor = findMentorByMember(member);
        MentorSlot mentorSlot = findMentorSlot(slotId);

        validateOwner(mentorSlot, mentor);
        // 예약 기록 존재 여부 검증 (모든 예약 기록 확인)
        validateNoReservationHistory(slotId);

        mentorSlotRepository.delete(mentorSlot);
    }


    // ===== 헬퍼 메서드 =====

    private Mentor findMentorByMember(Member member) {
        return mentorRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTOR));
    }

    private Mentoring findMentoring(Mentor mentor) {
        List<Mentoring> mentorings = mentoringRepository.findByMentorId(mentor.getId());
        if (mentorings.isEmpty()) {
            throw new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING);
        }
        return mentorings.getFirst();
    }

    private MentorSlot findMentorSlot(Long slotId) {
        return mentorSlotRepository.findById(slotId)
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));
    }


    // ===== 검증 메서드 =====

    private static void validateOwner(MentorSlot mentorSlot, Mentor mentor) {
        if (!mentorSlot.isOwnerBy(mentor)) {
            throw new ServiceException(MentorSlotErrorCode.NOT_OWNER);
        }
    }

    private void validateMentorExists(Long mentorId) {
        if (!mentorRepository.existsById(mentorId)) {
            throw new ServiceException(MentoringErrorCode.NOT_FOUND_MENTOR);
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
        if (reservationRepository.existsByMentorSlotId(slotId)) {
            throw new ServiceException(MentorSlotErrorCode.CANNOT_DELETE_RESERVED_SLOT);
        }
    }
}
