package com.back.domain.mentoring.reservation.service;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.service.DateTimeValidator;
import com.back.global.exception.ServiceException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MentoringStorage mentoringStorage;

    @Transactional
    public ReservationResponse createReservation(Mentee mentee, ReservationRequest reqDto) {
        try {
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

            mentorSlot.setReservation(reservation);

            reservationRepository.save(reservation);

            return ReservationResponse.from(reservation);
        } catch (OptimisticLockException e) {
            throw new ServiceException(ReservationErrorCode.CONCURRENT_RESERVATION_CONFLICT);
        }
    }


    // ===== 검증 메서드 =====

    private void validateMentorSlotStatus(MentorSlot mentorSlot, Mentee mentee) {
        Optional<Reservation> existingReservation  = reservationRepository.findByMentorSlotIdAndStatusIn(
            mentorSlot.getId(),
            List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.COMPLETED)
        );

        if (existingReservation.isPresent()) {
            if (existingReservation.get().isMentee(mentee)) {
                throw new ServiceException(ReservationErrorCode.ALREADY_RESERVED_SLOT);
            }
            throw new ServiceException(ReservationErrorCode.NOT_AVAILABLE_SLOT);
        }
    }
}
