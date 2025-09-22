package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.LoginRequest;
import com.back.domain.member.member.dto.SignupRequest;
import com.back.domain.member.member.dto.MenteeSignupRequest;
import com.back.domain.member.member.dto.MentorSignupRequest;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "MemberController", description = "회원 컨트롤러")
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @PostMapping("/signup/mentee")
    @Operation(summary = "멘티 회원가입")
    public RsData<Void> signupMentee(@RequestBody MenteeSignupRequest request) {
        try {
            Member member = memberService.joinMentee(
                request.email(),
                request.name(),
                request.password(),
                request.interestedField()
            );
            return new RsData<>("200-1", "멘티 회원가입 성공");
        } catch (IllegalArgumentException e) {
            return new RsData<>("400-1", e.getMessage());
        }
    }

    @PostMapping("/signup/mentor")
    @Operation(summary = "멘토 회원가입")
    public RsData<Void> signupMentor(@RequestBody MentorSignupRequest request) {
        try {
            Member member = memberService.joinMentor(
                request.email(),
                request.name(),
                request.password(),
                request.career(),
                request.careerYears()
            );
            return new RsData<>("200-1", "멘토 회원가입 성공");
        } catch (IllegalArgumentException e) {
            return new RsData<>("400-1", e.getMessage());
        }
    }

    @PostMapping("/login")
    public RsData<Void> login(@RequestBody LoginRequest request) {
        System.out.println("=== LOGIN REQUEST RECEIVED ===");
        String email = request.getEmail();
        String password = request.getPassword();
        System.out.println("Email: " + email + ", Password: " + password);
        Optional<Member> memberOpt = memberService.findByEmail(email);
        System.out.println("Member found: " + memberOpt.isPresent());
        
        if (memberOpt.isEmpty()) {
            return new RsData<>("400-1", "존재하지 않는 이메일입니다.");
        }

        Member member = memberOpt.get();
        
        memberService.checkPassword(member, password);

        // JWT 토큰 생성 후 쿠키에 저장
        String accessToken = memberService.genAccessToken(member);
        String refreshToken = memberService.genRefreshToken(member);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        return new RsData<>("200-1", "로그인 성공");
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
            return new RsData<>("401-1", "로그인이 필요합니다.");
        }

        return new RsData<>("200-1", "사용자 정보 조회 성공", actor);
    }

    @PostMapping("/refresh")
    public RsData<Void> refresh() {
        String refreshToken = rq.getCookieValue("refreshToken", "");

        if (refreshToken.isBlank()) {
            return new RsData<>("401-1", "Refresh token이 없습니다.");
        }

        // Refresh token 유효성 검증
        if (!memberService.isValidToken(refreshToken)) {
            return new RsData<>("401-2", "유효하지 않은 refresh token입니다.");
        }

        // Refresh token인지 확인
        if (!memberService.isRefreshToken(refreshToken)) {
            return new RsData<>("401-3", "Access token으로는 갱신할 수 없습니다.");
        }

        // Refresh token에서 사용자 정보 추출
        Map<String, Object> payload = memberService.payload(refreshToken);
        if (payload == null) {
            return new RsData<>("401-4", "토큰에서 사용자 정보를 추출할 수 없습니다.");
        }

        String email = (String) payload.get("email");
        Optional<Member> memberOpt = memberService.findByEmail(email);
        if (memberOpt.isEmpty()) {
            return new RsData<>("401-5", "존재하지 않는 사용자입니다.");
        }

        Member member = memberOpt.get();

        // 새로운 access token 생성
        String newAccessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", newAccessToken);

        return new RsData<>("200-1", "토큰 갱신 성공");
    }
}
