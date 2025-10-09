package com.back.domain.mentoring.session.repository;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.entity.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
    Optional<MentoringSession> findByMentoring(Mentoring mentoring);
    void deleteByReservation(Reservation reservation);
    Optional<MentoringSession> findBySessionUrl(String mentoringSessionUUid);
    Optional<MentoringSession> findByReservation(Reservation reservation);
}
