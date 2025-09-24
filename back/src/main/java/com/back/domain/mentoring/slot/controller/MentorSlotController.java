package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.service.MentorSlotService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentor-slot")
@RequiredArgsConstructor
@Tag(name = "MentorSlotController", description = "멘토 슬롯(멘토의 예약 가능 일정) API")
public class MentorSlotController {

    private final MentorSlotService mentorSlotService;
    private final Rq rq;

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "멘토 슬롯 생성", description = "멘토 슬롯을 생성합니다. 로그인한 멘토만 생성할 수 있습니다.")
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
    @Operation(summary = "멘토 슬롯 수정", description = "멘토 슬롯을 수정합니다. 멘토 슬롯 작성자만 접근할 수 있습니다.")
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
