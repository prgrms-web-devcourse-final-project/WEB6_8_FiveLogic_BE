package com.back.domain.mentoring.session.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.session.dto.*;
import com.back.domain.mentoring.session.service.MentoringSessionManager;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
@Tag(name = "MentoringSessionController", description = "멘토링 세션 API - 화상 채팅으로 멘토링 진행")
public class MentoringSessionController {
    private final MentoringSessionManager mentoringSessionManager;
    private final Rq rq;

    @GetMapping("/{sessionId}/url")
    @Operation(summary = "세션참여 URL 발급", description = "세션 참여를 위한 URL을 발급합니다.")
    public RsData<GetSessionUrlResponse> getSessionUrl(@PathVariable Long sessionId) {
        GetSessionUrlResponse response = mentoringSessionManager.getSessionUrl(sessionId);
        return new RsData<>("200", "요청완료", response);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "세션 상세 정보", description = "세션 제목, 멘토, 멘티, 메시지 목록 등 세션의 상세 정보를 조회합니다.")
    public RsData<GetSessionInfoResponse> getSessionDetail(@PathVariable Long sessionId) {
        GetSessionInfoResponse response = mentoringSessionManager.getSessionDetail(sessionId);
        return new RsData<>("200", "요청완료", response);
    }

    @PutMapping("/{sessionId}")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "세션 열기", description = "세션을 열어 멘토링을 진행합니다.")
    public RsData<OpenSessionResponse> openSession(@PathVariable Long sessionId) {
        Member member = rq.getActor();
        OpenSessionRequest openSessionRequest = new OpenSessionRequest(sessionId);
        OpenSessionResponse response = mentoringSessionManager.openSession(member, openSessionRequest);
        return new RsData<>("200", "세션 오픈 완료", response);
    }

    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "세션 종료", description = "세션을 닫아 멘토링을 종료합니다.")
    public RsData<CloseSessionResponse> closeSession(@PathVariable Long sessionId) {
        Member member = rq.getActor();
        DeleteSessionRequest deleteSessionRequest = new DeleteSessionRequest(sessionId);
        CloseSessionResponse response = mentoringSessionManager.closeSession(member, deleteSessionRequest);
        return new RsData<>("200", "세션 종료 완료", response);
    }
}
