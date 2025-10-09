package com.back.domain.mentoring.session.service;

import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.session.entity.MentoringSessionStatus;
import com.back.domain.mentoring.session.repository.MentoringSessionRepository;
import com.back.fixture.mentoring.MentoringSessionFixture;
import com.back.fixture.mentoring.ReservationFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MentoringSessionServiceTest {
    @Mock
    private MentoringSessionRepository mentoringSessionRepository;
    @InjectMocks
    private MentoringSessionService mentoringSessionService;

    @Test
    @DisplayName("승인된 예약으로 멘토링 세션을 생성할 수 있다.")
    void createMentoringSession() {
        Reservation reservation = ReservationFixture.createDefault();
        reservation.approve(reservation.getMentor());

        MentoringSession session = MentoringSession.create(reservation);
        when(mentoringSessionRepository.save(any(MentoringSession.class))).thenReturn(session);

        MentoringSession actualSession = mentoringSessionService.create(reservation);

        // then
        assertThat(actualSession).isNotNull();
        assertThat(actualSession.getReservation()).isEqualTo(reservation);
        assertThat(actualSession.getStatus()).isEqualTo(MentoringSessionStatus.CLOSED);
    }

    @Test
    @DisplayName("예약이 승인되지 않은 멘토링은 세션을 생성할 수 없다.")
    void cannotCreateMentoringSessionIfNotApproved() {
        Reservation reservation = ReservationFixture.createDefault();

        try {
            mentoringSessionService.create(reservation);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("Reservation must be APPROVED to create a MentoringSession.");
        }
    }

    @Test
    @DisplayName("멘토링 세션이 조회된다.")
    void getMentoringSession() {
        MentoringSession session = MentoringSessionFixture.createDefault();

        when(mentoringSessionRepository.findById(any()))
                .thenReturn(java.util.Optional.of(session));

        MentoringSession actualSession = mentoringSessionService.getMentoringSession(session.getId());

        assertThat(actualSession).isNotNull();
        assertThat(actualSession).isEqualTo(session);
    }

    @Test
    @DisplayName("존재하지 않는 멘토링 세션은 조회하면 예외가 발생한다.")
    void cannotGetNonExistentMentoringSession() {
        Long nonExistentId = 999L;

        when(mentoringSessionRepository.findById(any()))
                .thenReturn(java.util.Optional.empty());

        try {
            mentoringSessionService.getMentoringSession(nonExistentId);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ServiceException.class);
            assertThat(e.getMessage()).isEqualTo("404 : 잘못된 id");
        }
    }
}
