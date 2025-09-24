package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.mentoring.dto.MentoringDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringPagingResponse;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.service.MentoringService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentoring")
@RequiredArgsConstructor
public class MentoringController {
    private final MentoringService mentoringService;
    private final Rq rq;

    @GetMapping
    public RsData<MentoringPagingResponse> getMentorings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        Page<MentoringDto> mentoringPage = mentoringService.getMentorings(keyword, page, size);
        MentoringPagingResponse resDto = MentoringPagingResponse.from(mentoringPage);

        return new RsData<>(
            "200",
            "멘토링 목록을 조회하였습니다.",
            resDto
        );
    }

    @GetMapping("/{mentoringId}")
    public RsData<MentoringResponse> getMentoring(
        @PathVariable Long mentoringId
    ) {
        MentoringResponse resDto = mentoringService.getMentoring(mentoringId);

        return new RsData<>(
            "200",
            "멘토링을 조회하였습니다.",
            resDto
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentoringResponse> createMentoring(
        @RequestBody @Valid MentoringRequest reqDto
    ) {
        Member member = rq.getActor();
        MentoringResponse resDto = mentoringService.createMentoring(reqDto, member);

        return new RsData<>(
            "201",
            "멘토링이 생성 완료되었습니다.",
            resDto
        );
    }

    @PutMapping("/{mentoringId}")
    public RsData<MentoringResponse> updateMentoring(
        @PathVariable Long mentoringId,
        @RequestBody @Valid MentoringRequest reqDto
    ) {
        Member member = rq.getActor();
        MentoringResponse resDto = mentoringService.updateMentoring(mentoringId, reqDto, member);

        return new RsData<>(
            "200",
            "멘토링이 수정되었습니다.",
            resDto
        );
    }

    @DeleteMapping("/{mentoringId}")
    public RsData<Void> deleteMentoring(
        @PathVariable Long mentoringId
    ) {
        Member member = rq.getActor();
        mentoringService.deleteMentoring(mentoringId, member);

        return new RsData<>(
            "200",
            "멘토링이 삭제되었습니다."
        );
    }
}
