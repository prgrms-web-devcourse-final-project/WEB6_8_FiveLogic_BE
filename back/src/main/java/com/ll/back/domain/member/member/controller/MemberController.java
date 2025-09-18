package com.ll.back.domain.member.member.controller;

import com.ll.back.domain.member.member.dto.LoginRequest;
import com.ll.back.domain.member.member.dto.SignupRequest;
import com.ll.back.domain.member.member.entity.Member;
import com.ll.back.domain.member.member.service.MemberService;
import com.ll.back.global.rq.Rq;
import com.ll.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @PostMapping("/signup")
    public RsData<Void> signup(@RequestBody SignupRequest request) {
        try {
            Member member = memberService.join(
                request.getEmail(), 
                request.getName(), 
                request.getPassword(), 
                request.getRole()
            );
            return new RsData<>("200-1", "회원가입 성공");
        } catch (IllegalArgumentException e) {
            return new RsData<>("400-1", e.getMessage());
        }
    }

    @PostMapping("/login")
    public RsData<Void> login(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        Optional<Member> memberOpt = memberService.findByEmail(email);
        
        if (memberOpt.isEmpty()) {
            return new RsData<>("400-1", "존재하지 않는 이메일입니다.");
        }

        Member member = memberOpt.get();
        
        // TODO: 비밀번호 암호화 후 검증 로직 추가 필요
        if (!member.getPassword().equals(password)) {
            return new RsData<>("400-1", "비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성 후 쿠키에 저장
        String accessToken = memberService.genAccessToken(member);
        rq.setCookie("accessToken", accessToken);

        return new RsData<>("200-1", "로그인 성공");
    }

    @PostMapping("/logout")
    public RsData<Void> logout() {
        rq.deleteCookie("accessToken");
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
}
