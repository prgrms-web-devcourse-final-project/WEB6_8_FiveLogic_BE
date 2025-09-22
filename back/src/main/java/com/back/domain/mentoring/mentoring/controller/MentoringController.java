package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.mentoring.dto.request.MentoringCreateRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringCreateResponse;
import com.back.domain.mentoring.mentoring.service.MentoringService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mentoring")
@RequiredArgsConstructor
public class MentoringController {
    private final MentoringService mentoringService;
    private final Rq rq;

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentoringCreateResponse> createMentoring(
        @RequestBody @Valid MentoringCreateRequest reqDto
    ) {
        Member member = rq.getActor();
        MentoringCreateResponse resDto = mentoringService.createMentoring(reqDto, member);

        return new RsData<>(
            "201-1",
            "멘토링이 생성 완료되었습니다.",
            resDto
        );
    }
}
