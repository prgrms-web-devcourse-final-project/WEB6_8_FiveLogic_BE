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
@Tag(name = "MemberAuthController", description = "회원 인증 컨트롤러")
public class MemberAuthController {
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
    @Operation(summary = "로그인")
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
    @Operation(summary = "로그아웃")
    public RsData<Void> logout() {
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");
        return new RsData<>("200-8", "로그아웃 성공");
    }

    @GetMapping("/me")
    @Operation(summary = "사용자 정보 조회")
    public RsData<MemberMeResponse> me() {
        MemberMeResponse response = memberService.getMemberMe(rq.getActor());
        return new RsData<>("200-5", "사용자 정보 조회 성공", response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신")
    public RsData<Void> refresh() {
        String refreshToken = rq.getCookieValue("refreshToken", "");
        Member member = memberService.refreshAccessToken(refreshToken);

        // 새로운 access token 생성
        String newAccessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", newAccessToken);

        return new RsData<>("200-6", "토큰 갱신 성공");
    }

    @PostMapping("/me/withdraw")
    @Operation(summary = "회원 탈퇴")
    public RsData<Void> withdrawMember() {
        Member currentUser = rq.getActor();
        memberService.deleteMember(currentUser);

        // 탈퇴 후 쿠키 삭제
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return new RsData<>("200-7", "회원 탈퇴가 완료되었습니다.");
    }

    @GetMapping("/mentors")
    @Operation(summary = "모든 멘토 조회")
    public RsData<MentorPagingResponse> getAllMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MentorPagingResponse response = memberService.getAllMentors(page, size);
        return new RsData<>("200-8", "멘토 목록 조회 성공", response);
    }

}
