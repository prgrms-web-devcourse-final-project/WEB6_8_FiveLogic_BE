package com.back.domain.mentoring.reservation.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.MentorFixture;
import com.back.fixture.mentoring.MentorSlotFixture;
import com.back.fixture.mentoring.MentoringFixture;
import com.back.global.exception.ServiceException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private MentoringStorage mentoringStorage;

    private Mentor mentor;
    private Mentee mentee, mentee2;
    private Mentoring mentoring;
    private MentorSlot mentorSlot;

    @BeforeEach
    void setUp() {
        Member mentorMember = MemberFixture.create("mentor@test.com", "Mentor", "pass123");
        mentor = MentorFixture.create(1L, mentorMember);

        Member menteeMember = MemberFixture.create("mentee@test.com", "Mentee", "pass123");
        mentee = MenteeFixture.create(1L, menteeMember);

        Member menteeMember2 = MemberFixture.create("mentee2@test.com", "Mentee2", "pass123");
        mentee2 = MenteeFixture.create(2L, menteeMember2);

        mentoring = MentoringFixture.create(1L, mentor);
        mentorSlot = MentorSlotFixture.create(1L, mentor);
    }

    @Nested
    @DisplayName("멘토링 예약 생성")
    class Describe_createReservation {

        private ReservationRequest request;

        @BeforeEach
        void setUp() {
            request = new ReservationRequest(
                mentor.getId(),
                mentorSlot.getId(),
                mentoring.getId(),
                "사전 질문입니다."
            );
        }

        @Test
        @DisplayName("생성 성공")
        void createReservation() {
            // given
            when(mentoringStorage.findMentoring(request.mentoringId()))
                .thenReturn(mentoring);
            when(mentoringStorage.findMentorSlot(request.mentorSlotId()))
                .thenReturn(mentorSlot);
            when(reservationRepository.findByMentorSlotIdAndStatusIn(mentorSlot.getId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.COMPLETED)))
                .thenReturn(Optional.empty());

            // when
            ReservationResponse response = reservationService.createReservation(mentee, request);

            // then
            assertThat(response.mentoring().mentoringId()).isEqualTo(mentoring.getId());
            assertThat(response.mentee().menteeId()).isEqualTo(mentee.getId());
            assertThat(response.mentor().mentorId()).isEqualTo(mentor.getId());
            assertThat(response.reservation().mentorSlotId()).isEqualTo(mentorSlot.getId());
            assertThat(response.reservation().preQuestion()).isEqualTo(request.preQuestion());
            assertThat(response.reservation().status()).isEqualTo(ReservationStatus.PENDING);

            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        @DisplayName("이미 해당 멘티가 예약한 슬롯이면 예외")
        void throwExceptionWhenAlreadyReservedBySameMentee() {
            // given
            Reservation existingReservation = Reservation.builder()
                .mentee(mentee)
                .mentorSlot(mentorSlot)
                .mentoring(mentoring)
                .build();

            when(mentoringStorage.findMentoring(request.mentoringId()))
                .thenReturn(mentoring);
            when(mentoringStorage.findMentorSlot(request.mentorSlotId()))
                .thenReturn(mentorSlot);
            when(reservationRepository.findByMentorSlotIdAndStatusIn(mentorSlot.getId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.COMPLETED)))
                .thenReturn(Optional.of(existingReservation));

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(mentee, request))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.ALREADY_RESERVED_SLOT.getCode());
        }

        @Test
        @DisplayName("다른 멘티가 이미 예약한 슬롯이면 예외")
        void throwExceptionWhenSlotNotAvailable() {
            // given
            Reservation existingReservation = Reservation.builder()
                .mentee(mentee2) // 다른 멘티
                .mentorSlot(mentorSlot)
                .mentoring(mentoring)
                .build();

            when(mentoringStorage.findMentoring(request.mentoringId()))
                .thenReturn(mentoring);
            when(mentoringStorage.findMentorSlot(request.mentorSlotId()))
                .thenReturn(mentorSlot);
            when(reservationRepository.findByMentorSlotIdAndStatusIn(mentorSlot.getId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.COMPLETED)))
                .thenReturn(Optional.of(existingReservation));

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(mentee, request))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.NOT_AVAILABLE_SLOT.getCode());
        }

        @Test
        @DisplayName("예약 시간이 과거이면 예외")
        void throwExceptionWhenStartTimeInPast() {
            // given
            MentorSlot pastSlot = MentorSlotFixture.create(2L, mentor,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1));

            ReservationRequest pastRequest = new ReservationRequest(
                mentor.getId(),
                pastSlot.getId(),
                mentoring.getId(),
                "사전 질문입니다."
            );

            when(mentoringStorage.findMentoring(pastRequest.mentoringId()))
                .thenReturn(mentoring);
            when(mentoringStorage.findMentorSlot(pastRequest.mentorSlotId()))
                .thenReturn(pastSlot);
            when(reservationRepository.findByMentorSlotIdAndStatusIn(pastSlot.getId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.COMPLETED)))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(mentee, pastRequest))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.START_TIME_IN_PAST.getCode());
        }

        @Test
        @DisplayName("동시성 충돌 발생 시 예외")
        void throwExceptionOnConcurrentReservation() {
            // given
            when(mentoringStorage.findMentoring(request.mentoringId()))
                .thenReturn(mentoring);
            when(mentoringStorage.findMentorSlot(request.mentorSlotId()))
                .thenReturn(mentorSlot);
            when(reservationRepository.findByMentorSlotIdAndStatusIn(mentorSlot.getId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.COMPLETED)))
                .thenReturn(Optional.empty());

            // OptimisticLockException 테스트 위해 save 호출 시 예외 설정
            doThrow(new OptimisticLockException()).when(reservationRepository).save(any(Reservation.class));

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(mentee, request))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CONCURRENT_RESERVATION_CONFLICT.getCode());
        }
    }
}
