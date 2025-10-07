package com.back.domain.mentoring.session.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.session.dto.*;
import com.back.domain.mentoring.session.entity.MentoringSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringSessionManager {
    private final MentoringSessionService mentoringSessionService;
    private final MemberStorage memberStorage;

    public GetSessionUrlResponse getSessionUrl(Long sessionId) {
        MentoringSession session = mentoringSessionService.getMentoringSession(sessionId);
        return new GetSessionUrlResponse(session.getSessionUrl());
    }

    @Transactional
    public GetSessionInfoResponse getSessionDetail(Long sessionId) {
        MentoringSession session = mentoringSessionService.getMentoringSession(sessionId);
        Mentoring mentoring = session.getMentoring();
        Reservation reservation = session.getReservation();
        String sessionStatus = session.getStatus().toString();
        return new GetSessionInfoResponse(
                mentoring.getTitle(),
                reservation.getMentor().getMember().getNickname(),
                reservation.getMentee().getMember().getNickname(),
                sessionStatus
        );
    }

    @Transactional
    public OpenSessionResponse openSession(Member requestUser, OpenSessionRequest openSessionRequest) {
        Mentor mentor = memberStorage.findMentorByMember(requestUser);
        MentoringSession session = mentoringSessionService.getMentoringSession(openSessionRequest.sessionId());
        MentoringSession openedSession = mentoringSessionService.save(session.openSession(mentor));
        return new OpenSessionResponse(openedSession.getSessionUrl(), openedSession.getMentoring().getTitle(), openedSession.getStatus().toString());
    }

    @Transactional
    public CloseSessionResponse closeSession(Member requestUser, DeleteSessionRequest deleteRequest) {
        Mentor mentor = memberStorage.findMentorByMember(requestUser);
        MentoringSession session = mentoringSessionService.getMentoringSession(deleteRequest.sessionId());
        MentoringSession closedSession = mentoringSessionService.save(session.closeSession(mentor));
        return new CloseSessionResponse(closedSession.getSessionUrl(), closedSession.getMentoring().getTitle(), closedSession.getStatus().toString());
    }
}
