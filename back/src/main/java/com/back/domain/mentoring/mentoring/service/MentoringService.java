package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.mentor.dto.MentorDetailDto;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.MentoringDetailDto;
import com.back.domain.mentoring.mentoring.dto.MentoringWithTagsDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringService {
    private final MentoringRepository mentoringRepository;
    private final MentoringStorage mentoringStorage;

    @Transactional(readOnly = true)
    public Page<MentoringWithTagsDto> getMentorings(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return mentoringRepository.searchMentorings(keyword, pageable)
            .map(MentoringWithTagsDto::from);
    }

    @Transactional(readOnly = true)
    public MentoringResponse getMentoring(Long mentoringId)  {
        Mentoring mentoring = mentoringStorage.findMentoring(mentoringId);

        return new MentoringResponse(
            MentoringDetailDto.from(mentoring),
            MentorDetailDto.from(mentoring.getMentor())
        );
    }

    @Transactional
    public MentoringResponse createMentoring(MentoringRequest reqDto, Mentor mentor) {
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
            MentorDetailDto.from(mentor)
        );
    }

    @Transactional
    public MentoringResponse updateMentoring(Long mentoringId, MentoringRequest reqDto, Mentor mentor) {
        Mentoring mentoring = mentoringStorage.findMentoring(mentoringId);

        validateOwner(mentoring, mentor);

        mentoring.update(reqDto.title(), reqDto.bio(), reqDto.tags(), reqDto.thumb());

        return new MentoringResponse(
            MentoringDetailDto.from(mentoring),
            MentorDetailDto.from(mentor)
        );
    }

    @Transactional
    public void deleteMentoring(Long mentoringId, Mentor mentor) {
        Mentoring mentoring = mentoringStorage.findMentoring(mentoringId);

        validateOwner(mentoring, mentor);

        // 예약 이력이 있을 시 삭제 불가
        if (mentoringStorage.hasReservationsForMentoring(mentoring.getId())) {
            throw new ServiceException(MentoringErrorCode.CANNOT_DELETE_MENTORING);
        }

        // 멘토 슬롯 있을 시 일괄 삭제 (추후 1:N 변경 시 제거 필요)
        if (mentoringStorage.hasMentorSlotsForMentor(mentor.getId())) {
            mentoringStorage.deleteMentorSlotsData(mentor.getId());
        }
        mentoringRepository.delete(mentoring);
    }


    // ===== 유효성 검사 =====

    private void validateOwner(Mentoring mentoring, Mentor mentor) {
        if (!mentoring.isOwner(mentor)) {
            throw new ServiceException(MentoringErrorCode.FORBIDDEN_NOT_OWNER);
        }
    }
}
