package com.back.domain.mentoring.session.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.session.dto.*;
import com.back.domain.mentoring.session.service.MentoringSessionManager;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class MentoringSessionController {
    private final MentoringSessionManager mentoringSessionManager;
    private final Rq rq;

    //세션참여 URL발급
    @GetMapping("/{sessionId}/url")
    public RsData<GetSessionUrlResponse> getSessionUrl(@PathVariable Long sessionId) {
        GetSessionUrlResponse response = mentoringSessionManager.getSessionUrl(sessionId);
        return new RsData<>("200", "요청완료", response);
    }

    //세션 상세 정보(참여 현황?, 제목 등등?)
    @GetMapping("/{sessionId}")
    public RsData<GetSessionInfoResponse> getSessionDetail(@PathVariable Long sessionId) {
        GetSessionInfoResponse response = mentoringSessionManager.getSessionDetail(sessionId);
        return new RsData<>("200", "요청완료", response);
    }

    //세션 열기
    @PutMapping("/{sessionId}")
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<OpenSessionResponse> openSession(@PathVariable Long sessionId) {
        Member member = rq.getActor();
        OpenSessionRequest openSessionRequest = new OpenSessionRequest(sessionId);
        OpenSessionResponse response = mentoringSessionManager.openSession(member, openSessionRequest);
        return new RsData<>("200", "세션 오픈 완료", response);
    }

    //세션종료
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<CloseSessionResponse> closeSession(@PathVariable Long sessionId) {
        Member member = rq.getActor();
        DeleteSessionRequest deleteSessionRequest = new DeleteSessionRequest(sessionId);
        CloseSessionResponse response = mentoringSessionManager.closeSession(member, deleteSessionRequest);
        return new RsData<>("200", "세션 종료 완료", response);
    }
}
