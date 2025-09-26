package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.error.MemberErrorCode;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.repository.MentorRepository;
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

    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;

    private final MentoringRepository mentoringRepository;
    private final MentorSlotRepository mentorSlotRepository;
    private final ReservationRepository reservationRepository;

    public Mentee findMenteeByMember(Member member) {
        return menteeRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new ServiceException(MemberErrorCode.NOT_FOUND_MENTEE));
    }

    public Mentoring findMentoring(Long mentoringId) {
        return mentoringRepository.findById(mentoringId)
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING));
    }

    public MentorSlot findMentorSlot(Long slotId) {
        return mentorSlotRepository.findById(slotId)
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));
    }
}
