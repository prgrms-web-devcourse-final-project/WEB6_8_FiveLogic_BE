package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MenteeMyPageResponse;
import com.back.domain.member.member.dto.MenteeUpdateRequest;
import com.back.domain.member.member.dto.MentorMyPageResponse;
import com.back.domain.member.member.dto.MentorUpdateRequest;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "MemberMyPageController", description = "회원 마이페이지 컨트롤러")
public class MemberMyPageController {
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/me/mentee")
    @Operation(summary = "멘티 마이페이지 조회")
    public RsData<MenteeMyPageResponse> getMenteeMyPage() {
        Member currentUser = rq.getActor();
        MenteeMyPageResponse response = memberService.getMenteeMyPage(currentUser);
        return new RsData<>("200-9", "멘티 정보 조회 성공", response);
    }

    @PutMapping("/me/mentee")
    @Operation(summary = "멘티 정보 수정")
    public RsData<Void> updateMentee(@RequestBody MenteeUpdateRequest request) {
        Member currentUser = rq.getActor();
        memberService.updateMentee(currentUser, request);
        return new RsData<>("200-10", "멘티 정보 수정 성공");
    }

    @GetMapping("/me/mentor")
    @Operation(summary = "멘토 마이페이지 조회")
    public RsData<MentorMyPageResponse> getMentorMyPage() {
        Member currentUser = rq.getActor();
        MentorMyPageResponse response = memberService.getMentorMyPage(currentUser);
        return new RsData<>("200-11", "멘토 정보 조회 성공", response);
    }

    @PutMapping("/me/mentor")
    @Operation(summary = "멘토 정보 수정")
    public RsData<Void> updateMentor(@RequestBody MentorUpdateRequest request) {
        Member currentUser = rq.getActor();
        memberService.updateMentor(currentUser, request);
        return new RsData<>("200-12", "멘토 정보 수정 성공");
    }
}