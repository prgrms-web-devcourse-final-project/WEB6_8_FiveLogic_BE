package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.service.MentorSlotService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentor-slot")
@RequiredArgsConstructor
public class MentorSlotController {

    private final MentorSlotService mentorSlotService;
    private final Rq rq;

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentorSlotResponse> createMentorSlot(
        @RequestBody @Valid MentorSlotRequest reqDto
    ) {
        Member member = rq.getActor();
        MentorSlotResponse resDto = mentorSlotService.createMentorSlot(reqDto, member);

        return new RsData<>(
            "201",
            "멘토링 예약 일정을 등록했습니다.",
            resDto
        );
    }

    @PutMapping("/{slotId}")
    public RsData<MentorSlotResponse> updateMentorSlot(
        @PathVariable Long slotId,
        @RequestBody @Valid MentorSlotRequest reqDto
    ) {
        Member member = rq.getActor();
        MentorSlotResponse resDto = mentorSlotService.updateMentorSlot(slotId, reqDto, member);

        return new RsData<>(
            "200",
            "멘토링 예약 일정이 수정되었습니다.",
            resDto
        );
    }

}
