package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.mentoring.mentoring.dto.MentorDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDetailDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringService {
    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;
    private final ReservationRepository reservationRepository;
    private final MentorSlotRepository mentorSlotRepository;

    @Transactional
    public MentoringResponse getMentoring(Long mentoringId)  {
        Mentoring mentoring = findMentoring(mentoringId);
        Mentor mentor = mentoring.getMentor();

        return new MentoringResponse(
            MentoringDetailDto.from(mentoring),
            MentorDto.from(mentor)
        );
    }

    @Transactional
    public MentoringResponse createMentoring(MentoringRequest reqDto, Member member) {
        Mentor mentor = findMentor(member);

        // 멘토당 멘토링 1개 제한 체크 (추후 1:N 변경 시 제거 필요)
        if (mentoringRepository.existsByMentorId(mentor.getId())) {
            throw new ServiceException(MentoringErrorCode.ALREADY_EXISTS_MENTORING);
        }

        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(reqDto.title())
            .bio(reqDto.bio())
            .tags(reqDto.tags())
            .thumb(reqDto.thumb())
            .build();

        mentoringRepository.save(mentoring);

        return new MentoringResponse(
            MentoringDetailDto.from(mentoring),
            MentorDto.from(mentor)
        );
    }

    @Transactional
    public MentoringResponse updateMentoring(Long mentoringId, MentoringRequest reqDto, Member member) {
        Mentor mentor = findMentor(member);
        Mentoring mentoring = findMentoring(mentoringId);

        validateOwner(mentoring, mentor);

        mentoring.update(reqDto.title(), reqDto.bio(), reqDto.tags(), reqDto.thumb());

        return new MentoringResponse(
            MentoringDetailDto.from(mentoring),
            MentorDto.from(mentor)
        );
    }

    @Transactional
    public void deleteMentoring(Long mentoringId, Member member) {
        Mentor mentor = findMentor(member);
        Mentoring mentoring = findMentoring(mentoringId);

        validateOwner(mentoring, mentor);

        // 예약 이력이 있을 시 삭제 불가
        if (reservationRepository.existsByMentoringId(mentoring.getId())) {
            throw new ServiceException(MentoringErrorCode.CANNOT_DELETE_MENTORING);
        }

        // 멘토 슬롯 있을 시 일괄 삭제 (추후 1:N 변경 시 제거 필요)
        if (mentorSlotRepository.existsByMentorId(mentor.getId())) {
            mentorSlotRepository.deleteAllByMentorId(mentor.getId());
        }
        // 멘토 삭제
        mentoringRepository.delete(mentoring);
    }


    // ===== 헬퍼 메서드 =====

    private Mentor findMentor(Member member) {
        return mentorRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTOR));
    }

    private Mentoring findMentoring(Long mentoringId) {
        return mentoringRepository.findById(mentoringId)
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING));
    }


    // ===== 유효성 검사 =====

    private void validateOwner(Mentoring mentoring, Mentor mentor) {
        if (!mentoring.isOwner(mentor)) {
            throw new ServiceException(MentoringErrorCode.FORBIDDEN_NOT_OWNER);
        }
    }
}
