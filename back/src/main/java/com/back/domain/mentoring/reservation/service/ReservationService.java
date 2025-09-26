package com.back.domain.mentoring.reservation.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.service.DateTimeValidator;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MentoringStorage mentoringStorage;

    @Transactional
    public ReservationResponse createReservation(Member menteeMember, ReservationRequest reqDto) {
        Mentee mentee = mentoringStorage.findMenteeByMember(menteeMember);
        Mentoring mentoring = mentoringStorage.findMentoring(reqDto.mentoringId());
        MentorSlot mentorSlot = mentoringStorage.findMentorSlot(reqDto.mentorSlotId());

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
