package com.back.domain.mentoring.session.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.dto.*;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.slot.constant.MentorSlotStatus;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.MentorFixture;
import com.back.fixture.mentoring.MentoringFixture;
import com.back.fixture.mentoring.MentoringSessionFixture;
import com.back.fixture.mentoring.MentorSlotFixture;
import com.back.fixture.mentoring.ReservationFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MentoringSessionManagerTest {

    @Mock
    private MentoringSessionService mentoringSessionService;

    @Mock
    private MemberStorage memberStorage;

    @InjectMocks
    private MentoringSessionManager mentoringSessionManager;

    private Member mentorMember;
    private Mentor mentor;
    private Mentoring mentoring;
    private MentorSlot mentorSlot;
    private Reservation reservation;
    private MentoringSession mentoringSession;

    @BeforeEach
    void setUp() {
        // 테스트에 사용될 객체들을 فxture를 통해 생성
        reservation = ReservationFixture.createDefault();
        mentor = reservation.getMentor();
        mentorMember = mentor.getMember();
        mentoring = reservation.getMentoring();
        mentorSlot = reservation.getMentorSlot();
        mentoringSession = MentoringSessionFixture.create(reservation);
    }

    @Nested
    @DisplayName("세션 조회 테스트")
    class GetSessionTest {

        @Test
        @DisplayName("세션 URL을 성공적으로 조회한다.")
        void getSessionUrl_success() {
            // given
            Long sessionId = 1L;
            given(mentoringSessionService.getMentoringSession(sessionId)).willReturn(mentoringSession);

            // when
            GetSessionUrlResponse response = mentoringSessionManager.getSessionUrl(sessionId);

            // then
            assertThat(response.sessionUrl()).isEqualTo(mentoringSession.getSessionUrl());
        }

        @Test
        @DisplayName("세션 상세 정보를 성공적으로 조회한다.")
        void getSessionDetail_success() {
            // given
            Long sessionId = 1L;
            given(mentoringSessionService.getMentoringSession(sessionId)).willReturn(mentoringSession);

            // when
            GetSessionInfoResponse response = mentoringSessionManager.getSessionDetail(sessionId);

            // then
            assertThat(response.mentoringTitle()).isEqualTo(mentoring.getTitle());
            assertThat(response.mentorName()).isEqualTo(mentor.getMember().getNickname());
            assertThat(response.menteeName()).isEqualTo(reservation.getMentee().getMember().getNickname());
            assertThat(response.sessionStatus()).isEqualTo(mentoringSession.getStatus().toString());
        }
    }

    @Nested
    @DisplayName("세션 관리 테스트")
    class ManageSessionTest {

        @Test
        @DisplayName("멘토가 세션을 성공적으로 연다.")
        void openSession_success() {
            // given
            Long sessionId = 1L;
            OpenSessionRequest request = new OpenSessionRequest(sessionId);

            given(memberStorage.findMentorByMember(mentorMember)).willReturn(mentor);
            given(mentoringSessionService.getMentoringSession(sessionId)).willReturn(mentoringSession);
            // openSession은 상태만 변경하므로, 변경된 자기 자신을 반환하도록 설정
            given(mentoringSessionService.save(any(MentoringSession.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            OpenSessionResponse response = mentoringSessionManager.openSession(mentorMember, request);

            // then
            // openSession 후의 상태를 검증
            assertThat(response.status()).isEqualTo("OPEN");
            verify(mentoringSessionService).save(any(MentoringSession.class));
        }

        @Test
        @DisplayName("멘토가 세션을 성공적으로 닫는다.")
        void closeSession_success() {
            // given
            Long sessionId = 1L;
            DeleteSessionRequest request = new DeleteSessionRequest(sessionId);
            // 세션을 미리 OPEN 상태로 만듦
            mentoringSession.openSession(mentor);

            given(memberStorage.findMentorByMember(mentorMember)).willReturn(mentor);
            given(mentoringSessionService.getMentoringSession(sessionId)).willReturn(mentoringSession);
            // closeSession은 상태만 변경하므로, 변경된 자기 자신을 반환하도록 설정
            given(mentoringSessionService.save(any(MentoringSession.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            CloseSessionResponse response = mentoringSessionManager.closeSession(mentorMember, request);

            // then
            // closeSession 후의 상태를 검증
            assertThat(response.status()).isEqualTo("CLOSED");
            // reservation과 mentorSlot의 상태가 COMPLETED로 변경되었는지 검증
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
            assertThat(mentorSlot.getStatus()).isEqualTo(MentorSlotStatus.COMPLETED);
            verify(mentoringSessionService).save(any(MentoringSession.class));
        }
    }
}