package com.back.domain.mentoring.reservation.repository;

import com.back.domain.mentoring.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByMentoringId(Long mentoringId);
}
