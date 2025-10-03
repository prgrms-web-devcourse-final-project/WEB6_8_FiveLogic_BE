package com.back.domain.mentoring.session.service;

import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.entity.MentoringSession;
import com.back.domain.mentoring.session.repository.MentoringSessionRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentoringSessionService {
    private final MentoringSessionRepository mentoringSessionRepository;

    public MentoringSession create(Reservation reservation) {
        MentoringSession mentoringSession = MentoringSession.create(reservation);
        return mentoringSessionRepository.save(mentoringSession);
    }

    public MentoringSession getMentoringSession(Long id) {
        return mentoringSessionRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404", "잘못된 id"));
    }

    public MentoringSession getMentoringSessionByMentoring(Mentoring mentoring) {
        return mentoringSessionRepository.findByMentoring(mentoring)
                .orElseThrow(() -> new ServiceException("404", "해당 멘토링의 세션이 없습니다."));
    }

    public void deleteByReservation(Reservation reservation) {
        mentoringSessionRepository.deleteByReservation(reservation);
    }
}
