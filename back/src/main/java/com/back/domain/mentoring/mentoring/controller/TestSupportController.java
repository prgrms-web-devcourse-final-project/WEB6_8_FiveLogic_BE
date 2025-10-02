package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestSupportController {

    private final ReservationRepository reservationRepository;

    @PutMapping("/reservations/{reservationId}/complete")
    @Transactional
    public RsData<Void> completeReservation(@PathVariable Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ServiceException("400", "예약 없음"));

        //reservation.updateStatus(ReservationStatus.COMPLETED);

        return new RsData<>("200", "예약 상태가 완료로 변경되었습니다.");
    }
}
