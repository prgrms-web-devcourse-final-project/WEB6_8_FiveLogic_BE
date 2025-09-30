package com.back.domain.mentoring.reservation.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {
    private Mentor mentor, otherMentor;
    private Mentee mentee, otherMentee;
    private Mentoring mentoring;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Member mentorMember = MemberFixture.create("mentor@test.com", "Mentor", "pass123");
        mentor = MentorFixture.create(1L, mentorMember);

        Member otherMentorMember = MemberFixture.create("other@test.com", "Other", "pass123");
        otherMentor = MentorFixture.create(2L, otherMentorMember);

        Member menteeMember = MemberFixture.create("mentee@test.com", "Mentee", "pass123");
        mentee = MenteeFixture.create(1L, menteeMember);

        Member otherMenteeMember = MemberFixture.create("other_mentee@test.com", "OtherMentee", "pass123");
        otherMentee = MenteeFixture.create(2L, otherMenteeMember);

        mentoring = MentoringFixture.create(1L, mentor);
        MentorSlot mentorSlot = MentorSlotFixture.create(1L, mentor);
        reservation = ReservationFixture.create(1L, mentoring, mentee, mentorSlot);
    }

    @Nested
    @DisplayName("예약 수락")
    class Describe_approve {

        @Test
        @DisplayName("수락 성공")
        void approve() {
            // when
            reservation.approve(mentor);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
            assertThat(reservation.getMentorSlot().getStatus()).isEqualTo(MentorSlotStatus.APPROVED);
        }

        @Test
        @DisplayName("해당 예약의 멘토가 아닌 경우 예외")
        void throwExceptionWhenNotMentor() {
            // when & then
            assertThatThrownBy(() -> reservation.approve(otherMentor))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.FORBIDDEN_NOT_MENTOR.getCode());
        }

        @Test
        @DisplayName("요청 상태가 아니면 예외")
        void throwExceptionWhenNotPending() {
            // given
            reservation.approve(mentor);

            // when & then
            assertThatThrownBy(() -> reservation.approve(mentor))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_APPROVE.getCode());
        }

        @Test
        @DisplayName("이미 시작 시간이 지났으면 예외")
        void throwExceptionWhenSlotInPast() {
            // given
            MentorSlot pastSlot = MentorSlotFixture.create(2L, mentor,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1));

            Reservation pastReservation = Reservation.builder()
                .mentoring(mentoring)
                .mentee(mentee)
                .mentorSlot(pastSlot)
                .preQuestion("사전 질문")
                .build();

            // when & then
            assertThatThrownBy(() -> pastReservation.approve(mentor))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.INVALID_MENTOR_SLOT.getCode());
        }
    }

    @Nested
    @DisplayName("예약 거절")
    class Describe_reject {

        @Test
        @DisplayName("거절 성공")
        void reject() {
            // when
            reservation.reject(mentor);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);
            assertThat(reservation.getMentorSlot().getStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);
            assertThat(reservation.getMentorSlot().getReservation()).isNull();
        }

        @Test
        @DisplayName("요청 상태가 아니면 예외")
        void throwExceptionWhenNotPending() {
            // given
            reservation.approve(mentor);

            // when & then
            assertThatThrownBy(() -> reservation.reject(mentor))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_REJECT.getCode());
        }
    }

    @Nested
    @DisplayName("예약 취소 - 멘토")
    class Describe_cancelByMentor {

        @Test
        @DisplayName("취소 성공 - PENDING")
        void cancelPending() {
            // when
            reservation.cancel(mentor);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(reservation.getMentorSlot().getStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);
            assertThat(reservation.getMentorSlot().getReservation()).isNull();
        }

        @Test
        @DisplayName("취소 성공 - APPROVED")
        void cancelApproved() {
            // given
            reservation.approve(mentor);

            // when
            reservation.cancel(mentor);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(reservation.getMentorSlot().getStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);
            assertThat(reservation.getMentorSlot().getReservation()).isNull();
        }

        @Test
        @DisplayName("취소 불가능한 상태면 예외")
        void throwExceptionWhenCannotCancel() {
            // given
            reservation.approve(mentor);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.COMPLETED);

            // when & then
            assertThatThrownBy(() -> reservation.cancel(mentor))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_CANCEL.getCode());
        }
    }

    @Nested
    @DisplayName("예약 취소 - 멘티")
    class Describe_cancelByMentee {

        @Test
        @DisplayName("취소 성공 - PENDING")
        void cancelPending() {
            // when
            reservation.cancel(mentee);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(reservation.getMentorSlot().getStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);
            assertThat(reservation.getMentorSlot().getReservation()).isNull();
        }

        @Test
        @DisplayName("취소 성공 - APPROVED")
        void cancelApproved() {
            // given
            reservation.approve(mentor);

            // when
            reservation.cancel(mentee);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
            assertThat(reservation.getMentorSlot().getStatus()).isEqualTo(MentorSlotStatus.AVAILABLE);
            assertThat(reservation.getMentorSlot().getReservation()).isNull();
        }

        @Test
        @DisplayName("다른 멘티가 취소하려고 하면 예외")
        void throwExceptionWhenNotMentee() {
            assertThatThrownBy(() -> reservation.cancel(otherMentee))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.FORBIDDEN_NOT_MENTEE.getCode());
        }

        @Test
        @DisplayName("취소 불가능한 상태면 예외")
        void throwExceptionWhenCannotCancel() {
            // given
            reservation.approve(mentor);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.COMPLETED);

            // when & then
            assertThatThrownBy(() -> reservation.cancel(mentee))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReservationErrorCode.CANNOT_CANCEL.getCode());
        }
    }
}