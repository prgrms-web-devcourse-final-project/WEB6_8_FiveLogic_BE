package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.service.MentoringService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentoring")
@RequiredArgsConstructor
public class MentoringController {
    private final MentoringService mentoringService;
    private final Rq rq;

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentoringResponse> createMentoring(
        @RequestBody @Valid MentoringRequest reqDto
    ) {
        Member member = rq.getActor();
        MentoringResponse resDto = mentoringService.createMentoring(reqDto, member);

        return new RsData<>(
            "201-1",
            "멘토링이 생성 완료되었습니다.",
            resDto
        );
    }

    @PutMapping("/{mentoringId}")
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentoringResponse> updateMentoring(
        @PathVariable Long mentoringId,
        @RequestBody @Valid MentoringRequest reqDto
    ) {
        Member member = rq.getActor();
        MentoringResponse resDto = mentoringService.updateMentoring(mentoringId, reqDto, member);

        return new RsData<>(
            "200-1",
            "멘토링이 수정되었습니다.",
            resDto
        );
    }
}
