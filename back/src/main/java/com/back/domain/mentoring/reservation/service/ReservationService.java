package com.back.domain.mentoring.reservation.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.domain.mentoring.slot.service.DateTimeValidator;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MenteeRepository menteeRepository;
    private final MentoringRepository mentoringRepository;
    private final MentorSlotRepository mentorSlotRepository;

    public ReservationResponse createReservation(Member menteeMember, @Valid ReservationRequest reqDto) {
        Mentee mentee = findMenteeByMember(menteeMember);
        Mentoring mentoring = findMentoring(reqDto.mentoringId());
        MentorSlot mentorSlot = findMentorSlot(reqDto.mentorSlotId());

        validateMentorSlotStatus(mentorSlot, mentee);
        DateTimeValidator.validateStartTimeNotInPast(mentorSlot.getStartDateTime());

        Reservation reservation = Reservation.builder()
            .mentoring(mentoring)
            .mentee(mentee)
            .mentorSlot(mentorSlot)
            .preQuestion(reqDto.preQuestion())
            .build();
        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation);
    }


    // ===== 헬퍼 메서드 =====

    private Mentee findMenteeByMember(Member member) {
        return menteeRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTEE));
    }

    private Mentoring findMentoring(Long mentoringId) {
        return mentoringRepository.findById(mentoringId)
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING));
    }

    private MentorSlot findMentorSlot(Long slotId) {
        return mentorSlotRepository.findById(slotId)
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));
    }


    // ===== 검증 메서드 =====

    private static void validateMentorSlotStatus(MentorSlot mentorSlot, Mentee mentee) {
        if (!mentorSlot.getStatus().equals(MentorSlotStatus.AVAILABLE)) {
            if (mentorSlot.getReservation() != null &&
                mentorSlot.getReservation().isMentee(mentee)
            ) {
                throw new ServiceException(ReservationErrorCode.ALREADY_RESERVED_SLOT);
            }
            throw new ServiceException(ReservationErrorCode.NOT_AVAILABLE_SLOT);
        }
    }
}
