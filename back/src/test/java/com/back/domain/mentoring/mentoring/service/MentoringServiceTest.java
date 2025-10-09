package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.MentoringWithTagsDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.Tag;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.mentoring.repository.TagRepository;
import com.back.fixture.MemberFixture;
import com.back.fixture.MentorFixture;
import com.back.fixture.mentoring.MentoringFixture;
import com.back.fixture.mentoring.TagFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentoringServiceTest {

    @InjectMocks
    private MentoringService mentoringService;

    @Mock
    private MentoringRepository mentoringRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private MentoringStorage mentoringStorage;

    private Mentor mentor1, mentor2;
    private Mentoring mentoring1;
    private MentoringRequest request;

    @BeforeEach
    void setUp() {
        Member member1 = MemberFixture.create("mentor1@test.com", "Mentor1", "pass123");
        mentor1 = MentorFixture.create(1L, member1);
        mentoring1 = MentoringFixture.create(1L, mentor1);

        Member member2 = MemberFixture.create("mentor2@test.com", "Mentor2", "pass123");
        mentor2 = MentorFixture.create(2L, member2);

        request = new MentoringRequest(
            "Spring Boot 멘토링",
            List.of("Spring", "Java"),
            "Spring Boot를 활용한 백엔드 개발 입문",
            "https://example.com/thumb.jpg"
        );
    }

    @Nested
    @DisplayName("멘토링 다건 조회")
    class Describe_getMentorings {

        @Test
        @DisplayName("검색어 없이 일반 조회")
        void getMentorings() {
            // given
            Mentoring mentoring2 = MentoringFixture.create(2L, mentor2);

            Member member3 = MemberFixture.create("mentor3@test.com", "Mentor3", "pass123");
            Mentor mentor3 = MentorFixture.create(3L, member3);
            Mentoring mentoring3 = MentoringFixture.create(3L, mentor3);

            String keyword = "";
            Pageable pageable = PageRequest.of(0, 10);

            List<Mentoring> mentorings = List.of(mentoring1, mentoring2, mentoring3);
            Page<Mentoring> mentoringPage = new PageImpl<>(mentorings, pageable, mentorings.size());

            when(mentoringRepository.searchMentorings(keyword, pageable))
                .thenReturn(mentoringPage);

            // when
            Page<MentoringWithTagsDto> result = mentoringService.getMentorings(keyword, 0, 10);

            // then
            assertThat(result.getContent()).hasSize(3);
            verify(mentoringRepository).searchMentorings(keyword, pageable);
        }

        @Test
        @DisplayName("멘토명 검색")
        void searchByKeyword() {
            // given
            String keyword = "Mentor2";
            Pageable pageable = PageRequest.of(0, 10);

            List<Mentoring> mentorings = List.of(mentoring1);
            Page<Mentoring> mentoringPage = new PageImpl<>(mentorings, pageable, 1);

            when(mentoringRepository.searchMentorings(keyword, pageable))
                .thenReturn(mentoringPage);

            // when
            Page<MentoringWithTagsDto> result = mentoringService.getMentorings(keyword, 0, 10);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(mentoringRepository).searchMentorings(keyword, pageable);
        }

        @Test
        @DisplayName("검색 결과 없을 시 빈 페이지 반환")
        void returnEmptyPage() {
            // given
            String keyword = "NoMatch";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Mentoring> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(mentoringRepository.searchMentorings(keyword, pageable))
                .thenReturn(emptyPage);

            // when
            Page<MentoringWithTagsDto> result = mentoringService.getMentorings(keyword, 0, 10);

            // then
            assertThat(result.getContent()).isEmpty();
            verify(mentoringRepository).searchMentorings(keyword, pageable);
        }
    }

    @Nested
    @DisplayName("멘토링 조회")
    class Describe_getMentoring {

        @Test
        @DisplayName("조회 성공")
        void getMentoring() {
            // given
            Long mentoringId = 1L;

            when(mentoringStorage.findMentoring(mentoringId))
                .thenReturn(mentoring1);

            // when
            MentoringResponse result = mentoringService.getMentoring(mentoringId);

            // then
            assertThat(result).isNotNull();
            verify(mentoringStorage).findMentoring(mentoringId);
        }
    }

    @Nested
    @DisplayName("멘토링 생성")
    class Describe_createMentoring {

        @Test
        @DisplayName("생성 성공")
        void createMentoring() {
            List<Tag> tags = TagFixture.createDefaultTags();

            when(tagRepository.findByNameIn(request.tags()))
                .thenReturn(tags);
            when(mentoringRepository.existsByMentorIdAndTitle(mentor1.getId(), request.title()))
                .thenReturn(false);

            // when
            MentoringResponse result = mentoringService.createMentoring(request, mentor1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.mentoring().title()).isEqualTo(request.title());
            assertThat(result.mentoring().bio()).isEqualTo(request.bio());
            assertThat(result.mentoring().tags()).isEqualTo(request.tags());
            assertThat(result.mentoring().thumb()).isEqualTo(request.thumb());
            verify(mentoringRepository).existsByMentorIdAndTitle(mentor1.getId(), request.title());
            verify(tagRepository).findByNameIn(request.tags());
            verify(mentoringRepository).save(any(Mentoring.class));
        }

        @Test
        @DisplayName("해당 멘토에게 동일한 이름의 멘토링이 존재하면 예외")
        void throwExceptionWhenAlreadyExists() {
            // given
            when(mentoringRepository.existsByMentorIdAndTitle(mentor1.getId(), request.title()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> mentoringService.createMentoring(request, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentoringErrorCode.ALREADY_EXISTS_MENTORING.getCode());
            verify(mentoringRepository).existsByMentorIdAndTitle(mentor1.getId(), request.title());
            verify(mentoringRepository, never()).save(any(Mentoring.class));
        }
    }

    @Nested
    @DisplayName("멘토링 수정")
    class Describe_updateMentoring {

        @Test
        @DisplayName("수정 성공")
        void updateMentoring() {
            // given
            Long mentoringId = 1L;
            List<Tag> tags = TagFixture.createDefaultTags();

            when(tagRepository.findByNameIn(request.tags()))
                .thenReturn(tags);
            when(mentoringStorage.findMentoring(mentoringId))
                .thenReturn(mentoring1);

            // when
            MentoringResponse result = mentoringService.updateMentoring(mentoringId, request, mentor1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.mentoring().title()).isEqualTo(request.title());
            assertThat(result.mentoring().bio()).isEqualTo(request.bio());
            assertThat(result.mentoring().tags()).isEqualTo(request.tags());
            assertThat(result.mentoring().thumb()).isEqualTo(request.thumb());

            verify(mentoringStorage).findMentoring(mentoringId);
            verify(tagRepository).findByNameIn(request.tags());
        }

        @Test
        @DisplayName("소유자가 아니면 예외")
        void throwExceptionWhenNotOwner() {
            // given
            Long mentoringId = 1L;

            when(mentoringStorage.findMentoring(mentoringId))
                .thenReturn(mentoring1);

            // when & then
            assertThatThrownBy(() -> mentoringService.updateMentoring(mentoringId, request, mentor2))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentoringErrorCode.FORBIDDEN_NOT_OWNER.getCode());
        }
    }

    @Nested
    @DisplayName("멘토링 삭제")
    class Describe_deleteMentoring {

        @Test
        @DisplayName("관련 엔티티 없을 시 삭제 성공")
        void deleteMentoring() {
            // given
            Long mentoringId = 1L;

            when(mentoringStorage.findMentoring(mentoringId))
                .thenReturn(mentoring1);
            when(mentoringStorage.hasReservationsForMentoring(mentoringId))
                .thenReturn(false);

            // when
            mentoringService.deleteMentoring(mentoringId, mentor1);

            // then
            verify(mentoringStorage).findMentoring(mentoringId);
            verify(mentoringStorage).hasReservationsForMentoring(mentoringId);
            verify(mentoringRepository).delete(mentoring1);
        }

        @Test
        @DisplayName("소유자가 아니면 예외")
        void throwExceptionWhenNotOwner() {
            // given
            Long mentoringId = 1L;

            when(mentoringStorage.findMentoring(mentoringId))
                .thenReturn(mentoring1);

            // when & then
            assertThatThrownBy(() -> mentoringService.deleteMentoring(mentoringId, mentor2))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentoringErrorCode.FORBIDDEN_NOT_OWNER.getCode());

            verify(mentoringRepository, never()).delete(any(Mentoring.class));
        }

        @Test
        @DisplayName("예약 존재하면 예외")
        void throwExceptionWhenReservationsExist() {
            // given
            Long mentoringId = 1L;

            when(mentoringStorage.findMentoring(mentoringId))
                .thenReturn(mentoring1);
            when(mentoringStorage.hasReservationsForMentoring(mentoringId))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> mentoringService.deleteMentoring(mentoringId, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentoringErrorCode.CANNOT_DELETE_MENTORING.getCode());

            verify(mentoringStorage).findMentoring(mentoringId);
            verify(mentoringStorage).hasReservationsForMentoring(mentoringId);
            verify(mentoringRepository, never()).delete(any());
        }
    }
}