package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.mentor.dto.MentorDetailDto;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.MentoringDetailDto;
import com.back.domain.mentoring.mentoring.dto.MentoringWithTagsDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.Tag;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.mentoring.repository.TagRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentoringService {
    private final MentoringRepository mentoringRepository;
    private final MentoringStorage mentoringStorage;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<MentoringWithTagsDto> getMentorings(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return mentoringRepository.searchMentorings(keyword, pageable)
            .map(MentoringWithTagsDto::from);
    }

    @Transactional(readOnly = true)
    public List<MentoringWithTagsDto> getMyMentorings(Mentor mentor) {
        return mentoringRepository.findByMentorIdOrderByIdDesc(mentor.getId())
            .stream()
            .map(MentoringWithTagsDto::from)
            .toList();
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
        validateMentoringTitle(mentor.getId(), reqDto.title());

        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(reqDto.title())
            .bio(reqDto.bio())
            .thumb(reqDto.thumb())
            .build();

        List<Tag> tags = getOrCreateTags(reqDto.tags());
        mentoring.updateTags(tags);

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
        validateMentoringTitleForUpdate(mentor.getId(), reqDto.title(), mentoringId);

        List<Tag> tags = getOrCreateTags(reqDto.tags());

        mentoring.update(reqDto.title(), reqDto.bio(), tags, reqDto.thumb());

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
        mentoringRepository.delete(mentoring);
    }


    // ==== Tag 관리 =====

    private List<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        // 기존 태그 조회
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

        Set<String> existingNames = existingTags.stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());

        // 신규 태그 생성
        List<Tag> newTags = createNewTags(tagNames, existingNames);

        // 기존 태그 + 신규 태그
        List<Tag> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);

        return allTags;
    }

    private List<Tag> createNewTags(List<String> tagNames, Set<String> existingNames) {
        List<Tag> newTags = tagNames.stream()
            .filter(name -> !existingNames.contains(name))
            .map(name -> Tag.builder().name(name).build())
            .toList();

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
        }
        return newTags;
    }


    // ===== 유효성 검사 =====

    private void validateOwner(Mentoring mentoring, Mentor mentor) {
        if (!mentoring.isOwner(mentor)) {
            throw new ServiceException(MentoringErrorCode.FORBIDDEN_NOT_OWNER);
        }
    }

    private void validateMentoringTitle(Long mentorId, String title) {
        if (mentoringRepository.existsByMentorIdAndTitle(mentorId, title)) {
            throw new ServiceException(MentoringErrorCode.ALREADY_EXISTS_MENTORING);
        }
    }

    private void validateMentoringTitleForUpdate(Long mentorId, String title, Long mentoringId) {
        if (mentoringRepository.existsByMentorIdAndTitleAndIdNot(mentorId, title, mentoringId)) {
            throw new ServiceException(MentoringErrorCode.ALREADY_EXISTS_MENTORING);
        }
    }
}
