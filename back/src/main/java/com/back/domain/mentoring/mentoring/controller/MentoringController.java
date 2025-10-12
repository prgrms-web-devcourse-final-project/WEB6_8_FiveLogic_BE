package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.MentoringWithTagsDto;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringPagingResponse;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.service.MentoringService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/mentorings")
@RequiredArgsConstructor
@Tag(name = "MentoringController", description = "멘토링 API")
public class MentoringController {
    private final Rq rq;
    private final MentoringService mentoringService;
    private final MemberStorage memberStorage;

    @GetMapping
    @Operation(summary = "멘토링 목록 조회", description = "멘토링 목록을 조회합니다")
    public RsData<MentoringPagingResponse> getMentorings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        Page<MentoringWithTagsDto> mentoringPage = mentoringService.getMentorings(keyword, page, size);
        MentoringPagingResponse resDto = MentoringPagingResponse.from(mentoringPage);

        return new RsData<>(
            "200",
            "멘토링 목록을 조회하였습니다.",
            resDto
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "나의 멘토링 목록 조회", description = "나의 멘토링 목록을 조회합니다. 로그인한 멘토만 접근할 수 있습니다.")
    public RsData<List<MentoringWithTagsDto>> getMyMentorings() {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        List<MentoringWithTagsDto> resDto = mentoringService.getMyMentorings(mentor);

        return new RsData<>(
            "200",
            "나의 멘토링 목록을 조회하였습니다.",
            resDto
        );
    }

    @GetMapping("/{mentoringId}")
    @Operation(summary = "멘토링 상세 조회", description = "특정 멘토링을 상세 조회합니다.")
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "멘토링 생성", description = "멘토링을 생성합니다. 로그인한 멘토만 생성할 수 있습니다.")
    public RsData<MentoringResponse> createMentoring(
        @Parameter(content = @Content(mediaType = "application/json"))
        @RequestPart(value = "reqDto") @Valid MentoringRequest reqDto,
        @RequestPart(value = "thumb", required = false) MultipartFile thumb
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        MentoringResponse resDto = mentoringService.createMentoring(reqDto, thumb, mentor);

        return new RsData<>(
            "201",
            "멘토링이 생성 완료되었습니다.",
            resDto
        );
    }

    @PutMapping(value = "/{mentoringId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "멘토링 수정", description = "멘토링을 수정합니다. 멘토링 작성자만 접근할 수 있습니다.")
    public RsData<MentoringResponse> updateMentoring(
        @PathVariable Long mentoringId,
        @Parameter(content = @Content(mediaType = "application/json"))
        @RequestPart(value = "reqDto") @Valid MentoringRequest reqDto,
        @RequestPart(value = "thumb", required = false) MultipartFile thumb
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        MentoringResponse resDto = mentoringService.updateMentoring(mentoringId, reqDto, thumb, mentor);

        return new RsData<>(
            "200",
            "멘토링이 수정되었습니다.",
            resDto
        );
    }

    @DeleteMapping("/{mentoringId}")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "멘토링 삭제", description = "멘토링을 삭제합니다. 멘토링 작성자만 접근할 수 있습니다.")
    public RsData<Void> deleteMentoring(
        @PathVariable Long mentoringId
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        mentoringService.deleteMentoring(mentoringId, mentor);

        return new RsData<>(
            "200",
            "멘토링이 삭제되었습니다."
        );
    }
}
