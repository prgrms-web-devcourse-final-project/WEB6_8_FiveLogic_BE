package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.mentoring.mentoring.dto.MentorDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDetailDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringCreateRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringCreateResponse;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringService {
    private final MentoringRepository mentoringRepository;
    private final MentorRepository mentorRepository;

    public Mentoring getLastestMentoring() {
        return mentoringRepository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING));
    }

    @Transactional
    public MentoringCreateResponse createMentoring(MentoringCreateRequest reqDto, Member member) {
        Mentor mentor = getMentor(member);

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

        return new MentoringCreateResponse(
            MentoringDetailDto.from(mentoring),
            MentorDto.from(mentor)
        );
    }



    // ===== 헬퍼 메서드 =====

    private Mentor getMentor(Member member) {
        return mentorRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTOR));
    }
}
