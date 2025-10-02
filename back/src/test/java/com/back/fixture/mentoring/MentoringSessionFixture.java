package com.back.fixture.mentoring;

import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.entity.MentoringSession;

public class MentoringSessionFixture {
    public static MentoringSession createDefault() {
        Reservation reservation = ReservationFixture.createDefault();
        return create(reservation);
    }

    public static MentoringSession create(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            reservation.approve(reservation.getMentor());
        }
        return MentoringSession.create(reservation);
    }
}