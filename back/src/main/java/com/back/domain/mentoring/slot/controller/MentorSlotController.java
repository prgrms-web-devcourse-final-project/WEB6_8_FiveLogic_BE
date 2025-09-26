package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRepetitionRequest;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotDto;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.service.MentorSlotService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/mentor-slots")
@RequiredArgsConstructor
@Tag(name = "MentorSlotController", description = "멘토 슬롯(멘토의 예약 가능 일정) API")
public class MentorSlotController {

    private final Rq rq;
    private final MentorSlotService mentorSlotService;
    private final MemberStorage memberStorage;

    @GetMapping
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "멘토의 모든 슬롯 목록 조회", description = "멘토가 본인의 모든 슬롯(예약된 슬롯 포함) 목록을 조회합니다.")
    public RsData<List<MentorSlotDto>> getMyMentorSlots(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        List<MentorSlotDto> resDtoList = mentorSlotService.getMyMentorSlots(mentor, startDateTime, endDateTime);

        return new RsData<>(
            "200",
            "나의 모든 일정 목록을 조회하였습니다.",
            resDtoList
        );
    }

    @GetMapping("/available/{mentorId}")
    @Operation(summary = "멘토의 예약 가능한 슬롯 목록 조회", description = "멘티가 특정 멘토의 예약 가능한 슬롯 목록을 조회합니다.")
    public RsData<List<MentorSlotDto>> getAvailableMentorSlots(
        @PathVariable Long mentorId,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        if (!memberStorage.existsMentorById(mentorId)) {
            throw new ServiceException(MentoringErrorCode.NOT_FOUND_MENTOR);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        List<MentorSlotDto> resDtoList = mentorSlotService.getAvailableMentorSlots(mentorId, startDateTime, endDateTime);

        return new RsData<>(
            "200",
            "멘토의 예약 가능 일정 목록을 조회하였습니다.",
            resDtoList
        );
    }

    @GetMapping("/{slotId}")
    @Operation(summary = "멘토 슬롯 조회", description = "특정 멘토 슬롯을 조회합니다.")
    public RsData<MentorSlotResponse> getMentorSlot(
        @PathVariable Long slotId
    ) {
        MentorSlotResponse resDto = mentorSlotService.getMentorSlot(slotId);

        return new RsData<>(
            "200",
            "멘토의 예약 가능 일정을 조회하였습니다.",
            resDto
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "멘토 슬롯 생성", description = "멘토 슬롯을 생성합니다. 로그인한 멘토만 생성할 수 있습니다.")
    public RsData<MentorSlotResponse> createMentorSlot(
        @RequestBody @Valid MentorSlotRequest reqDto
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        MentorSlotResponse resDto = mentorSlotService.createMentorSlot(reqDto, mentor);

        return new RsData<>(
            "201",
            "멘토의 예약 가능 일정을 등록했습니다.",
            resDto
        );
    }

    @PostMapping("/repetition")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "반복 슬롯 생성", description = "멘토 슬롯을 반복 생성합니다. 로그인한 멘토만 생성할 수 있습니다.")
    public RsData<Void> createMentorSlotRepetition(
        @RequestBody @Valid MentorSlotRepetitionRequest reqDto
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        mentorSlotService.createMentorSlotRepetition(reqDto, mentor);

        return new RsData<>(
            "201",
            "반복 일정을 등록했습니다."
        );
    }

    @PutMapping("/{slotId}")
    @Operation(summary = "멘토 슬롯 수정", description = "멘토 슬롯을 수정합니다. 멘토 슬롯 작성자만 접근할 수 있습니다.")
    public RsData<MentorSlotResponse> updateMentorSlot(
        @PathVariable Long slotId,
        @RequestBody @Valid MentorSlotRequest reqDto
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        MentorSlotResponse resDto = mentorSlotService.updateMentorSlot(slotId, reqDto, mentor);

        return new RsData<>(
            "200",
            "멘토의 예약 가능 일정이 수정되었습니다.",
            resDto
        );
    }

    @DeleteMapping("/{slotId}")
    @Operation(summary = "멘토 슬롯 삭제", description = "멘토 슬롯을 삭제합니다. 멘토 슬롯 작성자만 접근할 수 있습니다.")
    public RsData<Void> deleteMentorSlot(
        @PathVariable Long slotId
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        mentorSlotService.deleteMentorSlot(slotId, mentor);

        return new RsData<>(
            "200",
            "멘토의 예약 가능 일정이 삭제되었습니다."
        );
    }
}
