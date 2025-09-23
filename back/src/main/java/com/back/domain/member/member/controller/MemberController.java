package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.LoginRequest;
import com.back.domain.member.member.dto.MenteeSignupRequest;
import com.back.domain.member.member.dto.MentorVerificationRequest;
import com.back.domain.member.member.dto.MentorSignupVerifyRequest;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.domain.member.member.verification.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "MemberController", description = "회원 컨트롤러")
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/signup/mentee")
    @Operation(summary = "멘티 회원가입")
    public RsData<Void> signupMentee(@RequestBody MenteeSignupRequest request) {
        memberService.joinMentee(
            request.email(),
            request.name(),
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
            request.password(),
            request.career(),
            request.careerYears()
        );
        return new RsData<>("200-3", "멘토 회원가입 성공");
    }

    @PostMapping("/login")
    public RsData<Void> login(@RequestBody LoginRequest request) {
        Member member = memberService.findByEmail(request.getEmail())
            .orElseThrow(() -> new ServiceException("400-3", "존재하지 않는 이메일입니다."));

        memberService.checkPassword(member, request.getPassword());

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
        return new RsData<>("200-1", "로그아웃 성공");
    }

    @GetMapping("/me")
    public RsData<Member> me() {
        Member actor = rq.getActor();

        if (actor == null) {
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }

        return new RsData<>("200-5", "사용자 정보 조회 성공", actor);
    }

    @PostMapping("/refresh")
    public RsData<Void> refresh() {
        String refreshToken = rq.getCookieValue("refreshToken", "");

        if (refreshToken.isBlank()) {
            throw new ServiceException("401-1", "Refresh token이 없습니다.");
        }

        // Refresh token 유효성 검증
        if (!memberService.isValidToken(refreshToken)) {
            throw new ServiceException("401-2", "유효하지 않은 refresh token입니다.");
        }

        // Refresh token인지 확인
        if (!memberService.isRefreshToken(refreshToken)) {
            throw new ServiceException("401-3", "Access token으로는 갱신할 수 없습니다.");
        }

        // Refresh token에서 사용자 정보 추출
        Map<String, Object> payload = memberService.payload(refreshToken);
        if (payload == null) {
            throw new ServiceException("401-4", "토큰에서 사용자 정보를 추출할 수 없습니다.");
        }

        String email = (String) payload.get("email");
        Member member = memberService.findByEmail(email)
            .orElseThrow(() -> new ServiceException("401-5", "존재하지 않는 사용자입니다."));

        // 새로운 access token 생성
        String newAccessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", newAccessToken);

        return new RsData<>("200-6", "토큰 갱신 성공");
    }
}
