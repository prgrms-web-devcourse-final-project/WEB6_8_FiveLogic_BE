package com.back.domain.mentoring.reservation.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.dto.ReservationDto;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.session.service.MentoringSessionService;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.service.DateTimeValidator;
import com.back.global.exception.ServiceException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MentoringStorage mentoringStorage;
    private final MentoringSessionService mentoringSessionService;

    @Transactional(readOnly = true)
    public Page<ReservationDto> getReservations(Member member, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Reservation> reservations;

        if (member.getRole() == Member.Role.MENTOR) {
            reservations = reservationRepository.findAllByMentorMember(member, pageable);
        } else {
            reservations = reservationRepository.findAllByMenteeMember(member, pageable);
        }
        return reservations.map(r -> {
            MentoringSession mentoringSession = mentoringSessionService.getMentoringSessionByReservation(r);
            return ReservationDto.from(r, mentoringSession);
        });
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Member member, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndMember(reservationId, member)
            .orElseThrow(() -> new ServiceException(ReservationErrorCode.RESERVATION_NOT_ACCESSIBLE));

        MentoringSession mentoringSession = mentoringSessionService.getMentoringSessionByReservation(reservation);

        return ReservationResponse.from(reservation, mentoringSession);
    }

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
            // flush 필요...?

            reservationRepository.save(reservation);

            return ReservationResponse.from(reservation);
        } catch (OptimisticLockException e) {
            throw new ServiceException(ReservationErrorCode.CONCURRENT_RESERVATION_CONFLICT);
        }
    }

    @Transactional
    public ReservationResponse approveReservation(Mentor mentor, Long reservationId) {
        try {
            Reservation reservation = mentoringStorage.findReservation(reservationId);

            reservation.approve(mentor);

            // 예약이 승인되면 세션을 생성한다.
            MentoringSession mentoringSession = mentoringSessionService.create(reservation);

            return ReservationResponse.from(reservation);
        } catch (OptimisticLockException e) {
            throw new ServiceException(ReservationErrorCode.CONCURRENT_APPROVAL_CONFLICT);
        }
    }

    @Transactional
    public ReservationResponse rejectReservation(Mentor mentor, Long reservationId) {
        Reservation reservation = mentoringStorage.findReservation(reservationId);
        reservation.reject(mentor);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Member member, Long reservationId) {
        Reservation reservation = mentoringStorage.findReservation(reservationId);
        reservation.cancel(member);

        // 예약이 취소되면 세션을 제거한다.
        mentoringSessionService.deleteByReservation(reservation);
        return ReservationResponse.from(reservation);
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
