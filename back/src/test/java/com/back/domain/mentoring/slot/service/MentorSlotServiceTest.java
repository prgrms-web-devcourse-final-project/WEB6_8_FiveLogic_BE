package com.back.domain.mentoring.slot.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRepetitionRequest;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotDto;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.MentorFixture;
import com.back.fixture.mentoring.MentorSlotFixture;
import com.back.fixture.mentoring.MentoringFixture;
import com.back.fixture.mentoring.ReservationFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorSlotServiceTest {

    @InjectMocks
    private MentorSlotService mentorSlotService;

    @Mock
    private MentorSlotRepository mentorSlotRepository;

    @Mock
    private MentoringStorage mentoringStorage;

    private Mentor mentor1, mentor2;
    private Mentoring mentoring1;
    private MentorSlot mentorSlot1;
    private Mentee mentee1;

    @BeforeEach
    void setUp() {
        Member mentorMember1 = MemberFixture.create("mentor1@test.com", "Mentor1", "pass123");
        mentor1 = MentorFixture.create(1L, mentorMember1);
        mentoring1 = MentoringFixture.create(1L, mentor1);
        mentorSlot1 = MentorSlotFixture.create(1L, mentor1);

        Member mentorMember2 = MemberFixture.create("mentor2@test.com", "Mentor2", "pass123");
        mentor2 = MentorFixture.create(2L, mentorMember2);

        Member menteeMember1 = MemberFixture.create("mentee1@test.com", "Mentee1", "pass123");
        mentee1 = MenteeFixture.create(1L, menteeMember1);
    }

    @Nested
    @DisplayName("나의 멘토 슬롯 목록 조회")
    class Describe_getMyMentorSlots {

        @Test
        @DisplayName("조회 성공")
        void getMyMentorSlots() {
            // given
            LocalDate  base = LocalDate.now().plusMonths(1);
            LocalDateTime startDate = base.atStartOfDay();
            LocalDateTime endDate = base.withDayOfMonth(base.lengthOfMonth()).atTime(23, 59);

            MentorSlot slot2 = MentorSlotFixture.create(2L, mentor1,
                base.withDayOfMonth(2).atTime(10, 0),
                base.withDayOfMonth(2).atTime(11, 0));
            MentorSlot slot3 = MentorSlotFixture.create(3L, mentor1,
                    base.withDayOfMonth(15).atTime(14, 0),
                    base.withDayOfMonth(15).atTime(15, 0));

            List<MentorSlot> slots = List.of(mentorSlot1, slot2, slot3);

            when(mentorSlotRepository.findMySlots(mentor1.getId(), startDate, endDate))
                .thenReturn(slots);

            // when
            List<MentorSlotDto> result = mentorSlotService.getMyMentorSlots(mentor1, startDate, endDate);

            // then
            assertThat(result).hasSize(3);
            verify(mentorSlotRepository).findMySlots(mentor1.getId(), startDate, endDate);
        }

        @Test
        @DisplayName("조회 결과 없을 시 빈 리스트 반환")
        void returnEmptyList() {
            // given
            LocalDate  base = LocalDate.now().minusMonths(2);
            LocalDateTime startDate = base.atStartOfDay();
            LocalDateTime endDate = base.withDayOfMonth(base.lengthOfMonth()).atTime(23, 59);

            when(mentorSlotRepository.findMySlots(mentor1.getId(), startDate, endDate))
                .thenReturn(List.of());

            // when
            List<MentorSlotDto> result = mentorSlotService.getMyMentorSlots(mentor1, startDate, endDate);

            // then
            assertThat(result).isEmpty();
            verify(mentorSlotRepository).findMySlots(mentor1.getId(), startDate, endDate);
        }
    }

    @Nested
    @DisplayName("예약 가능한 멘토 슬롯 목록 조회")
    class Describe_getAvailableMentorSlots {

        @Test
        @DisplayName("조회 성공")
        void getAvailableMentorSlots() {
            // given
            LocalDate  base = LocalDate.now().plusMonths(1);
            LocalDateTime startDate = base.atStartOfDay();
            LocalDateTime endDate = base.withDayOfMonth(base.lengthOfMonth()).atTime(23, 59);

            MentorSlot slot2 = MentorSlotFixture.create(2L, mentor1,
                base.withDayOfMonth(2).atTime(10, 0),
                base.withDayOfMonth(2).atTime(11, 0));

            List<MentorSlot> availableSlots = List.of(mentorSlot1, slot2);

            when(mentorSlotRepository.findAvailableSlots(mentor1.getId(), startDate, endDate))
                .thenReturn(availableSlots);

            // when
            List<MentorSlotDto> result = mentorSlotService.getAvailableMentorSlots(mentor1.getId(), startDate, endDate);

            // then
            assertThat(result).hasSize(2);
            verify(mentorSlotRepository).findAvailableSlots(mentor1.getId(), startDate, endDate);
        }
    }

    @Nested
    @DisplayName("멘토 슬롯 단건 조회")
    class Describe_getMentorSlot {

        @Test
        @DisplayName("조회 성공")
        void getMentorSlot() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);
            when(mentoringStorage.findMentoringByMentor(mentor1))
                .thenReturn(mentoring1);

            // when
            MentorSlotResponse result = mentorSlotService.getMentorSlot(slotId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.mentorSlot().mentorSlotId()).isEqualTo(slotId);
            assertThat(result.mentor().mentorId()).isEqualTo(mentor1.getId());
            verify(mentoringStorage).findMentorSlot(slotId);
            verify(mentoringStorage).findMentoringByMentor(mentor1);
        }
    }

    @Nested
    @DisplayName("멘토 슬롯 생성")
    class Describe_createMentorSlot {

        private MentorSlotRequest request;

        @BeforeEach
        void setUp() {
            LocalDateTime baseDateTime = LocalDateTime.of(
                LocalDate.now().plusWeeks(1),
                LocalTime.of(10, 0, 0)
            ).truncatedTo(ChronoUnit.SECONDS);

            request = new MentorSlotRequest(
                mentor1.getId(),
                baseDateTime,
                baseDateTime.plusHours(2)
            );
        }

        @Test
        @DisplayName("생성 성공")
        void createMentorSlot() {
            // given
            when(mentoringStorage.findMentoringByMentor(mentor1))
                .thenReturn(mentoring1);
            when(mentorSlotRepository.existsOverlappingSlot(
                mentor1.getId(), request.startDateTime(), request.endDateTime()))
                .thenReturn(false);

            // when
            MentorSlotResponse result = mentorSlotService.createMentorSlot(request, mentor1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.mentor().mentorId()).isEqualTo(mentor1.getId());
            assertThat(result.mentoring().mentoringId()).isEqualTo(mentoring1.getId());
            assertThat(result.mentoring().title()).isEqualTo(mentoring1.getTitle());
            assertThat(result.mentorSlot().startDateTime()).isEqualTo(request.startDateTime());
            assertThat(result.mentorSlot().endDateTime()).isEqualTo(request.endDateTime());
            assertThat(result.mentorSlot().mentorSlotStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);

            verify(mentoringStorage).findMentoringByMentor(mentor1);
            verify(mentorSlotRepository).existsOverlappingSlot(mentor1.getId(), request.startDateTime(), request.endDateTime());
            verify(mentorSlotRepository).save(any(MentorSlot.class));
        }

        @Test
        @DisplayName("기존 슬롯과 시간 겹치면 예외")
        void throwExceptionWhenOverlapping() {
            // given
            when(mentoringStorage.findMentoringByMentor(mentor1))
                .thenReturn(mentoring1);
            when(mentorSlotRepository.existsOverlappingSlot(
                mentor1.getId(), request.startDateTime(), request.endDateTime()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.createMentorSlot(request, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.OVERLAPPING_SLOT.getCode());
            verify(mentorSlotRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("멘토 슬롯 반복 생성")
    class Describe_createMentorSlotRepetition {

        @Test
        @DisplayName("반복 생성 성공")
        void createMentorSlotRepetition() {
            // given
            LocalDate startDate = LocalDate.now().plusWeeks(1);
            LocalDate endDate = startDate.plusMonths(1);
            List<DayOfWeek> repeatDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

            MentorSlotRepetitionRequest request = new MentorSlotRepetitionRequest(
                startDate,
                endDate,
                repeatDays,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
            );

            when(mentorSlotRepository.existsOverlappingSlot(any(), any(), any()))
                .thenReturn(false);

            // when
            mentorSlotService.createMentorSlotRepetition(request, mentor1);

            // then
            verify(mentorSlotRepository).saveAll(argThat(slots -> {
                List<MentorSlot> slotList = new ArrayList<>();
                slots.forEach(slotList::add);

                // 개수 검증: startDate ~ endDate 사이 repeatDays가 몇 번 나오는지 계산
                long expectedCount = startDate.datesUntil(endDate.plusDays(1))
                    .filter(d -> repeatDays.contains(d.getDayOfWeek()))
                    .count();
                if (slotList.size() != expectedCount) return false;

                // 요일 검증
                Set<DayOfWeek> daysOfWeek = slotList.stream()
                    .map(slot -> slot.getStartDateTime().getDayOfWeek())
                    .collect(Collectors.toSet());
                if (!daysOfWeek.equals(new HashSet<>(repeatDays))) return false;

                // 시간, 멘토 검증
                return slotList.stream().allMatch(slot ->
                    slot.getStartDateTime().toLocalTime().equals(LocalTime.of(10, 0)) &&
                        slot.getEndDateTime().toLocalTime().equals(LocalTime.of(11, 0)) &&
                        slot.getMentor().equals(mentor1)
                );
            }));
        }

        @Test
        @DisplayName("반복 생성 중 겹치는 슬롯 있으면 예외")
        void throwExceptionWhenOverlapping() {
            // given
            LocalDate startDate = LocalDate.now().plusWeeks(1);
            LocalDate endDate = startDate.plusDays(6);

            MentorSlotRepetitionRequest request = new MentorSlotRepetitionRequest(
                startDate,
                endDate,
                List.of(DayOfWeek.MONDAY),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
            );

            when(mentorSlotRepository.existsOverlappingSlot(any(), any(), any()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.createMentorSlotRepetition(request, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.OVERLAPPING_SLOT.getCode());
        }
    }

    @Nested
    @DisplayName("멘토 슬롯 수정")
    class Describe_updateMentorSlot {

        private MentorSlotRequest request;

        @BeforeEach
        void setUp() {
            LocalDateTime baseDateTime = LocalDateTime.of(
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0, 0)
            ).truncatedTo(ChronoUnit.SECONDS);

            request = new MentorSlotRequest(
                mentor1.getId(),
                baseDateTime,
                baseDateTime.plusHours(1)
            );
        }

        @Test
        @DisplayName("수정 성공")
        void updateMentorSlot() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentoringByMentor(mentor1))
                .thenReturn(mentoring1);
            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);
            when(mentorSlotRepository.existsOverlappingExcept(mentor1.getId(), slotId, request.startDateTime(), request.endDateTime()))
                .thenReturn(false);

            // when
            MentorSlotResponse result = mentorSlotService.updateMentorSlot(slotId, request, mentor1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.mentorSlot().mentorSlotId()).isEqualTo(slotId);
            assertThat(result.mentor().mentorId()).isEqualTo(mentor1.getId());
            assertThat(result.mentoring().mentoringId()).isEqualTo(mentoring1.getId());
            assertThat(result.mentoring().title()).isEqualTo(mentoring1.getTitle());
            assertThat(result.mentorSlot().startDateTime()).isEqualTo(request.startDateTime());
            assertThat(result.mentorSlot().endDateTime()).isEqualTo(request.endDateTime());
            assertThat(result.mentorSlot().mentorSlotStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);

            verify(mentoringStorage).findMentorSlot(slotId);
            verify(mentorSlotRepository).existsOverlappingExcept(
                mentor1.getId(), slotId, request.startDateTime(), request.endDateTime());
        }

        @Test
        @DisplayName("소유자가 아니면 예외")
        void throwExceptionWhenNotOwner() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentoringByMentor(mentor2))
                .thenReturn(mentoring1);
            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.updateMentorSlot(slotId, request, mentor2))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.NOT_OWNER.getCode());
        }

        @Test
        @DisplayName("활성화된 예약이 있으면 예외")
        void throwExceptionWhenReserved() {
            // given
            Long slotId = 1L;
            Reservation reservation = ReservationFixture.create(1L, mentoring1, mentee1, mentorSlot1);
            mentorSlot1.setReservation(reservation);

            when(mentoringStorage.findMentoringByMentor(mentor1))
                .thenReturn(mentoring1);
            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.updateMentorSlot(slotId, request, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.CANNOT_UPDATE_RESERVED_SLOT.getCode());
        }

        @Test
        @DisplayName("다른 슬롯과 시간 겹치면 예외")
        void throwExceptionWhenOverlapping() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentoringByMentor(mentor1))
                .thenReturn(mentoring1);
            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);
            when(mentorSlotRepository.existsOverlappingExcept(
                mentor1.getId(), slotId, request.startDateTime(), request.endDateTime()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.updateMentorSlot(slotId, request, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.OVERLAPPING_SLOT.getCode());
        }
    }

    @Nested
    @DisplayName("멘토 슬롯 삭제")
    class Describe_deleteMentorSlot {

        @Test
        @DisplayName("삭제 성공")
        void deleteMentorSlot() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);
            when(mentoringStorage.hasReservationForMentorSlot(slotId))
                .thenReturn(false);

            // when
            mentorSlotService.deleteMentorSlot(slotId, mentor1);

            // then
            verify(mentoringStorage).findMentorSlot(slotId);
            verify(mentoringStorage).hasReservationForMentorSlot(slotId);
            verify(mentorSlotRepository).delete(mentorSlot1);
        }

        @Test
        @DisplayName("소유자가 아니면 예외")
        void throwExceptionWhenNotOwner() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.deleteMentorSlot(slotId, mentor2))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.NOT_OWNER.getCode());
            verify(mentorSlotRepository, never()).delete(any());
        }

        @Test
        @DisplayName("예약 기록이 있으면 예외")
        void throwExceptionWhenReservationHistoryExists() {
            // given
            Long slotId = 1L;

            when(mentoringStorage.findMentorSlot(slotId))
                .thenReturn(mentorSlot1);
            when(mentoringStorage.hasReservationForMentorSlot(slotId))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> mentorSlotService.deleteMentorSlot(slotId, mentor1))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.CANNOT_DELETE_RESERVED_SLOT.getCode());
            verify(mentoringStorage).findMentorSlot(slotId);
            verify(mentoringStorage).hasReservationForMentorSlot(slotId);
            verify(mentorSlotRepository, never()).delete(any());
        }
    }
}