package com.back.domain.mentoring.mentoring.service;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    public MentorSlot findMentorSlot(Long slotId) {
        return mentorSlotRepository.findById(slotId)
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));
    }


    // ==== exists 메서드 =====

    public boolean hasReservationsForMentoring(Long mentoringId) {
        return reservationRepository.existsByMentoringId(mentoringId);
    }

    public boolean hasMentorSlotsForMentor(Long mentorId) {
        return mentorSlotRepository.existsByMentorId(mentorId);
    }


    // ===== 데이터 조작 메서드 =====

    public void deleteMentorSlotsData(Long mentorId) {
        mentorSlotRepository.deleteAllByMentorId(mentorId);
    }
}
