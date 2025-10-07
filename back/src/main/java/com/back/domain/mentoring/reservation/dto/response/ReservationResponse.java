package com.back.domain.mentoring.reservation.dto.response;

import com.back.domain.member.mentee.dto.MenteeDto;
import com.back.domain.member.mentor.dto.MentorDto;
import com.back.domain.mentoring.mentoring.dto.MentoringDto;
import com.back.domain.mentoring.reservation.dto.ReservationDetailDto;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.entity.MentoringSession;

public record ReservationResponse(
    ReservationDetailDto reservation,
    MentoringDto mentoring,
    MentorDto mentor,
    MenteeDto mentee
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            ReservationDetailDto.from(reservation),
            MentoringDto.from(reservation.getMentoring()),
            MentorDto.from(reservation.getMentor()),
            MenteeDto.from(reservation.getMentee())
        );
    }
    public static ReservationResponse from(Reservation reservation, MentoringSession mentoringSession) {
        return new ReservationResponse(
                ReservationDetailDto.from(reservation, mentoringSession),
                MentoringDto.from(reservation.getMentoring()),
                MentorDto.from(reservation.getMentor()),
                MenteeDto.from(reservation.getMentee())
        );
    }
}
