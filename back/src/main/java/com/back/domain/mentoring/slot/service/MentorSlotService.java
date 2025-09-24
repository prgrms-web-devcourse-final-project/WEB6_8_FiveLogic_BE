package com.back.domain.mentoring.slot.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
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

    @Transactional
    public MentorSlotResponse createMentorSlot(MentorSlotRequest reqDto, Member member) {
        Mentor mentor = findMentor(member);
        Mentoring mentoring = findMentoring(mentor);

        // 시간대 유효성 검사
        MentorSlotValidator.validateTimeSlot(reqDto.startDateTime(), reqDto.endDateTime());

        // 기존 슬롯과 시간 겹치는지 검사
        validateOverlappingSlots(mentor, reqDto.startDateTime(), reqDto.endDateTime());

        MentorSlot mentorSlot = MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(reqDto.startDateTime())
            .endDateTime(reqDto.endDateTime())
            .build();

        mentorSlotRepository.save(mentorSlot);

        return MentorSlotResponse.from(mentorSlot, mentoring);
    }


    // ===== 헬퍼 메서드 =====

    private Mentor findMentor(Member member) {
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


    // ===== 검증 메서드 =====

    private void validateOverlappingSlots(Mentor mentor, LocalDateTime start, LocalDateTime end) {
        if (mentorSlotRepository.existsOverlappingSlot(mentor.getId(), start, end)) {
            throw new ServiceException(MentorSlotErrorCode.OVERLAPPING_SLOT);
        }
    }
}
