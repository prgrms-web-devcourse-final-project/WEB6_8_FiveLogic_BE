package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mentoring 관련 도메인들의 데이터 접근을 캡슐화
 * - 도메인 간 직접 Repository 참조를 방지
 * - 연관 엔티티 조회/검증/삭제 로직 제공
 */
@Component
@RequiredArgsConstructor
public class MentoringStorage {

    private final MentoringRepository mentoringRepository;
    private final MentorSlotRepository mentorSlotRepository;
    private final ReservationRepository reservationRepository;

    // ===== find 메서드 =====

    public Mentoring findMentoring(Long mentoringId) {
        return mentoringRepository.findById(mentoringId)
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING));
    }

    // TODO : 멘토:멘토링 1:N으로 변경 시 삭제 예정
    public Mentoring findMentoringByMentor(Mentor mentor) {
        return findMentoringsByMentorId(mentor.getId()).getFirst();
    }

    public List<Mentoring> findMentoringsByMentorId(Long mentorId) {
        List<Mentoring> mentorings = mentoringRepository.findByMentorId(mentorId);
        if (mentorings.isEmpty()) {
            throw new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING);
        }
        return mentorings;
    }

    public MentorSlot findMentorSlot(Long slotId) {
        return mentorSlotRepository.findById(slotId)
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));
    }

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ServiceException(ReservationErrorCode.NOT_FOUND_RESERVATION));
    }


    // ==== exists 메서드 =====

    public boolean hasReservationsForMentoring(Long mentoringId) {
        return reservationRepository.existsByMentoringId(mentoringId);
    }

    public boolean hasMentorSlotsForMentor(Long mentorId) {
        return mentorSlotRepository.existsByMentorId(mentorId);
    }

    public boolean hasReservationForMentorSlot(Long slotId) {
        return reservationRepository.existsByMentorSlotId(slotId);
    }


    // ===== 데이터 조작 메서드 =====

    public void deleteMentorSlotsData(Long mentorId) {
        mentorSlotRepository.deleteAllByMentorId(mentorId);
    }
}
