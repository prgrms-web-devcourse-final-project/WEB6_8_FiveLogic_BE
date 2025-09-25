package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.domain.member.member.verification.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "MemberController", description = "회원 컨트롤러")
public class  MemberController {
    private final MemberService memberService;
    private final Rq rq;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/signup/mentee")
    @Operation(summary = "멘티 회원가입")
    public RsData<Void> signupMentee(@RequestBody MenteeSignupRequest request) {
        memberService.joinMentee(
            request.email(),
            request.name(),
            request.nickname(),
            request.password(),
            request.interestedField()
        );
        return new RsData<>("200-1", "멘티 회원가입 성공");
    }

    @PostMapping("/signup/mentor/send-verification")
    @Operation(summary = "멘토 회원가입 인증번호 발송")
    public RsData<Void> sendMentorVerification(@RequestBody MentorVerificationRequest request) {
        emailVerificationService.generateAndSendCode(request.email());
        return new RsData<>("200-2", "인증번호가 발송되었습니다.");
    }

    @PostMapping("/signup/mentor")
    @Operation(summary = "멘토 회원가입 (인증번호 확인)")
    public RsData<Void> signupMentor(@RequestBody MentorSignupVerifyRequest request) {
        // 인증번호 검증
        emailVerificationService.verifyCode(request.email(), request.verificationCode());

        // 회원가입 진행
        memberService.joinMentor(
            request.email(),
            request.name(),
            request.nickname(),
            request.password(),
            request.career(),
            request.careerYears()
        );
        return new RsData<>("200-3", "멘토 회원가입 성공");
    }

    @PostMapping("/login")
    public RsData<Void> login(@RequestBody LoginRequest request) {
        Member member = memberService.login(request.email(), request.password());

        // JWT 토큰 생성 후 쿠키에 저장
        String accessToken = memberService.genAccessToken(member);
        String refreshToken = memberService.genRefreshToken(member);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        return new RsData<>("200-4", "로그인 성공");
    }

    @PostMapping("/logout")
    public RsData<Void> logout() {
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");
        return new RsData<>("200-8", "로그아웃 성공");
    }

    @GetMapping("/me")
    public RsData<Member> me() {
        Member actor = memberService.getCurrentUser(rq.getActor());
        return new RsData<>("200-5", "사용자 정보 조회 성공", actor);
    }

    @PostMapping("/refresh")
    public RsData<Void> refresh() {
        String refreshToken = rq.getCookieValue("refreshToken", "");
        Member member = memberService.refreshAccessToken(refreshToken);

        // 새로운 access token 생성
        String newAccessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", newAccessToken);

        return new RsData<>("200-6", "토큰 갱신 성공");
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴")
    public RsData<Void> deleteMember() {
        Member currentUser = rq.getActor();
        memberService.deleteMember(currentUser);

        // 탈퇴 후 쿠키 삭제
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return new RsData<>("200-7", "회원 탈퇴가 완료되었습니다.");
    }

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
