package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberPagingResponse;
import com.back.domain.member.member.dto.MemberSearchResponse;
import com.back.domain.member.member.dto.MentorUpdateRequest;
import com.back.domain.member.member.dto.MenteeUpdateRequest;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "AdmMemberController", description = "관리자 회원 관리 컨트롤러")
public class AdmMemberController {
    private final MemberService memberService;
    private final Rq rq;


    @GetMapping
    @Operation(summary = "회원 목록 조회 (관리자) - 페이징, 페이지는 기본으로 10개씩 조회")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<MemberPagingResponse> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        MemberPagingResponse members = memberService.getAllMembersForAdmin(page, size);
        return new RsData<>("200-18", "회원 목록 조회 성공", members);
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 상세 조회 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<MemberSearchResponse> getMember(@PathVariable Long memberId) {
        MemberSearchResponse member = memberService.getMemberForAdmin(memberId);
        return new RsData<>("200-14", "회원 상세 조회 성공", member);
    }

    @PostMapping("/{memberId}/delete")
    @Operation(summary = "회원 삭제 (관리자) - 소프트 삭제")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<Void> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMemberByAdmin(memberId);
        return new RsData<>("200-15", "회원 삭제 성공");
    }

    @PutMapping("/{memberId}/mentor")
    @Operation(summary = "멘토 정보 수정 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<Void> updateMentor(@PathVariable Long memberId, @RequestBody MentorUpdateRequest request) {
        memberService.updateMemberByAdmin(memberId, null, request.nickname(), null,
                request.career(), request.careerYears(), null);
        return new RsData<>("200-16", "멘토 정보 수정 성공");
    }

    @PutMapping("/{memberId}/mentee")
    @Operation(summary = "멘티 정보 수정 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<Void> updateMentee(@PathVariable Long memberId, @RequestBody MenteeUpdateRequest request) {
        memberService.updateMemberByAdmin(memberId, null, request.nickname(), null,
                null, null, request.interestedField());
        return new RsData<>("200-17", "멘티 정보 수정 성공");
    }
}