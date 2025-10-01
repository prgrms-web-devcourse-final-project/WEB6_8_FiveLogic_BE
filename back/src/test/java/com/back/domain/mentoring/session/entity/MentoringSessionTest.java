package com.back.domain.mentoring.session.entity;


import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.fixture.ReservationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MentoringSessionTest {
    @Test
    @DisplayName("APPROVED된 Reservation에 대해서 MentoringSession 생성")
    void mentoringSessionCreationTest() {
        Reservation reservation = ReservationFixture.createDefault();
        reservation.updateStatus(ReservationStatus.APPROVED);

        MentoringSession mentoringSession = MentoringSession.create(reservation);

        assertThat(mentoringSession).isNotNull();
        assertThat(mentoringSession.getSessionUrl()).isNotNull();
        assertThat(mentoringSession.getReservation()).isEqualTo(reservation);
        assertThat(mentoringSession.getMentoring()).isEqualTo(reservation.getMentoring());
        assertThat(mentoringSession.getStatus()).isEqualTo(MentoringSessionStatus.CLOSED);
    }

    @Test
    @DisplayName("APPROVED되지않은 Reservation에 대해 MentoringSession을 생성하려하면 에러를 반환한,")
    void mentoringSessionCreationWithInvalidReservationTest() {
        Reservation reservation = ReservationFixture.createDefault();

        try {
            MentoringSession.create(reservation);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Reservation must be APPROVED to create a MentoringSession.");
        }
    }

    @Test
    @DisplayName("MentoringSession의 상태를 OPEN으로 변경할 수 있다.")
    void mentoringSessionOpenTest() {
        Reservation reservation = ReservationFixture.createDefault();
        reservation.updateStatus(ReservationStatus.APPROVED);
        MentoringSession mentoringSession = MentoringSession.create(reservation);

        mentoringSession.openSession();
        assertThat(mentoringSession.getStatus()).isEqualTo(MentoringSessionStatus.OPEN);
    }

    @Test
    @DisplayName("MentoringSession의 상태를 CLOSED로 변경할 수 있다.")
    void mentoringSessionClosedTest() {
        Reservation reservation = ReservationFixture.createDefault();
        reservation.updateStatus(ReservationStatus.APPROVED);
        MentoringSession mentoringSession = MentoringSession.create(reservation);

        mentoringSession.closeSession();
        assertThat(mentoringSession.getStatus()).isEqualTo(MentoringSessionStatus.CLOSED);
    }

}
