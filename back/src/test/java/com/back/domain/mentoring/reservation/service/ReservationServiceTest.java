package com.back.domain.mentoring.reservation.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringStorage;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.dto.ReservationDto;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.session.service.MentoringSessionService;
import com.back.fixture.mentoring.MentoringSessionFixture;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.MentorFixture;
import com.back.fixture.mentoring.MentorSlotFixture;
import com.back.fixture.mentoring.MentoringFixture;
import com.back.fixture.mentoring.ReservationFixture;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Mock
    private MentoringSessionService mentoringSessionService;

    private Mentor mentor;
    private Mentee mentee, mentee2;
    private Mentoring mentoring;
    private MentorSlot mentorSlot, mentorSlot2;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Member mentorMember = MemberFixture.create(1L, "mentor@test.com", "Mentor", "pass123", Member.Role.MENTOR);
        mentor = MentorFixture.create(1L, mentorMember);

        Member menteeMember = MemberFixture.create(2L, "mentee@test.com", "Mentee", "pass123", Member.Role.MENTEE);
        mentee = MenteeFixture.create(1L, menteeMember);

        Member menteeMember2 = MemberFixture.create(3L, "mentee2@test.com", "Mentee2", "pass123", Member.Role.MENTEE);
        mentee2 = MenteeFixture.create(2L, menteeMember2);

        mentoring = MentoringFixture.create(1L, mentor);
        mentorSlot = MentorSlotFixture.create(1L, mentor);
        mentorSlot2 = MentorSlotFixture.create(2L, mentor);
        reservation = ReservationFixture.create(1L, mentoring, mentee, mentorSlot2);
    }

    @Nested
    @DisplayName("멘토링 예약 목록 조회")
    class Describe_getReservations {

        @Test
        void getReservations() {
            // given
            int page = 1;
            int size = 5;
            Pageable pageable = PageRequest.of(page, size);

            Page<Reservation> reservationPage = new PageImpl<>(
                List.of(reservation),
                pageable,
                10
            );

            when(reservationRepository.findAllByMentorMember(mentor.getMember().getId(), pageable))
                .thenReturn(reservationPage);

            // when
            Page<ReservationDto> result = reservationService.getReservations(
                mentor.getMember(),
                page,
                size
            );

            // then
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
            verify(reservationRepository).findAllByMentorMember(mentor.getMember().getId(), pageable);
        }
    }

    @Nested
    @DisplayName("멘토링 예약 조회")
    class Describe_getReservation {

        @Test
        void getReservation() {
            // given
            Long reservationId = reservation.getId();

            when(reservationRepository.findByIdAndMember(reservationId, mentor.getMember().getId()))
                .thenReturn(Optional.of(reservation));
            MentoringSession session = MentoringSessionFixture.create(reservation);
            when(mentoringSessionService.getMentoringSessionByReservation(reservation)).thenReturn(session);

            // when
            ReservationResponse response = reservationService.getReservation(
                mentor.getMember(),
                reservationId
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.mentoring().mentoringId()).isEqualTo(mentoring.getId());
            assertThat(response.mentee().menteeId()).isEqualTo(mentee.getId());
            assertThat(response.mentor().mentorId()).isEqualTo(mentor.getId());
            assertThat(response.reservation().mentorSlotId()).isEqualTo(mentorSlot2.getId());
            verify(reservationRepository).findByIdAndMember(reservationId, mentor.getMember().getId());
        }

        @Test
        @DisplayName("권한이 없을 경우 예외")
        void getReservation_notAccessible() {
            // given
            when(reservationRepository.findByIdAndMember(reservation.getId(), mentee2.getMember().getId()))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.getReservation(mentee2.getMember(), reservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode",
                    ReservationErrorCode.RESERVATION_NOT_ACCESSIBLE.getCode());
        }
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

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(mentee, pastRequest))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", MentorSlotErrorCode.START_TIME_IN_PAST.getCode());
        }
    }

    @Nested
    @DisplayName("예약 수락")
    class Describe_approveReservation {

        @Test
        @DisplayName("예약 수락 성공")
        void approveReservation() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.APPROVED);
            when(mentoringSessionService.create(any())).thenReturn(MentoringSessionFixture.create(reservation));
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);

            // when
            ReservationResponse result = reservationService.approveReservation(mentor, reservation.getId());

            // then
            assertThat(result.reservation().status()).isEqualTo(ReservationStatus.APPROVED);
            assertThat(result.reservation().mentorSlotId()).isEqualTo(mentorSlot2.getId());
            assertThat(result.mentor().mentorId()).isEqualTo(mentor.getId());
            assertThat(result.mentee().menteeId()).isEqualTo(mentee.getId());
        }

        @Test
        @DisplayName("PENDING 상태가 아니면 수락 불가")
        void throwExceptionWhenAlreadyApproved() {
            // given
            reservation.approve(mentor);

            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reservationService.approveReservation(mentor, reservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_APPROVE.getCode());
        }

        @Test
        @DisplayName("이미 시작 시간이 지난 슬롯은 수락 불가")
        void throwExceptionWhenMentorSlotInPast() {
            // given
            MentorSlot pastSlot = MentorSlotFixture.create(3L, mentor,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1));

            Reservation pastReservation = ReservationFixture.create(2L, mentoring, mentee, pastSlot);

            when(mentoringStorage.findReservation(pastReservation.getId()))
                .thenReturn(pastReservation);

            // when
            assertThatThrownBy(() -> reservationService.approveReservation(mentor, pastReservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.INVALID_MENTOR_SLOT.getCode());
        }

        @Test
        @DisplayName("동시성 충돌 발생 시 예외")
        void throwExceptionOnConcurrentApproval() {
            // given
            Reservation mockReservation = spy(reservation);

            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(mockReservation);

            // approve() 호출 시 OptimisticLockException 발생
            doThrow(new OptimisticLockException("다른 트랜잭션이 먼저 수락했습니다"))
                .when(mockReservation).approve(mentor);

            // when & then
            assertThatThrownBy(() -> reservationService.approveReservation(mentor, reservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CONCURRENT_APPROVAL_CONFLICT.getCode());
        }
    }

    @Nested
    @DisplayName("예약 거절")
    class Describe_rejectReservation {

        @Test
        @DisplayName("예약 거절 성공")
        void rejectReservation() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when
            ReservationResponse result = reservationService.rejectReservation(mentor, reservation.getId());

            // then
            assertThat(result.reservation().status()).isEqualTo(ReservationStatus.REJECTED);
            assertThat(result.reservation().mentorSlotId()).isEqualTo(mentorSlot2.getId());
            assertThat(result.mentor().mentorId()).isEqualTo(mentor.getId());
            assertThat(result.mentee().menteeId()).isEqualTo(mentee.getId());
        }

        @Test
        @DisplayName("PENDING 상태가 아니면 거절 불가")
        void throwExceptionWhenNotPending() {
            // given
            reservation.approve(mentor);

            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reservationService.rejectReservation(mentor, reservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_REJECT.getCode());
        }
    }

    @Nested
    @DisplayName("예약 취소")
    class Describe_cancelReservation {

        @Test
        @DisplayName("멘토가 예약 취소 성공")
        void cancelReservationByMentor() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when
            ReservationResponse result = reservationService.cancelReservation(mentor.getMember(), reservation.getId());

            // then
            assertThat(result.reservation().status()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("멘티가 예약 취소 성공")
        void cancelReservationByMentee() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when
            ReservationResponse result = reservationService.cancelReservation(mentee.getMember(), reservation.getId());

            // then
            assertThat(result.reservation().status()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("COMPLETED 상태는 취소 불가")
        void throwExceptionWhenCompleted() {
            // given
            // 완료 상태의 과거 슬롯
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS);
            MentorSlot pastSlot = MentorSlotFixture.create(3L, mentor, pastTime, pastTime.plusHours(1));
            Reservation completedReservation = ReservationFixture.create(2L, mentoring, mentee, pastSlot);

            // PENDING -> APPROVED는 미래 시간에 해야 하므로 리플렉션으로 직접 상태 변경
            ReflectionTestUtils.setField(completedReservation, "status", ReservationStatus.APPROVED);
            completedReservation.complete();

            when(mentoringStorage.findReservation(completedReservation.getId()))
                .thenReturn(completedReservation);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(mentor.getMember(), completedReservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_CANCEL.getCode());
        }

        @Test
        @DisplayName("다른 멘토는 취소 불가")
        void throwExceptionWhenNotMentor() {
            // given
            Member anotherMentorMember = MemberFixture.create("another@test.com", "Another", "pass123");
            Mentor anotherMentor = MentorFixture.create(2L, anotherMentorMember);
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(anotherMentor.getMember(), reservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.FORBIDDEN_NOT_PARTICIPANT.getCode());
        }

        @Test
        @DisplayName("다른 멘티는 취소 불가")
        void throwExceptionWhenNotMentee() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(mentee2.getMember(), reservation.getId()))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.FORBIDDEN_NOT_PARTICIPANT.getCode());
        }
    }
}
