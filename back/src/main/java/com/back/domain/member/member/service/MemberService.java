package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;

    public Member join(String email, String name, String password, Member.Role role) {
        memberRepository.findByEmail(email).ifPresent(
                member -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                }
        );

        Member member = new Member(email, password, name, role);

        return memberRepository.save(member);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }

    public Map<String, Object> payload(String accessToken) {
        return authTokenService.payload(accessToken);
    }

    public String genRefreshToken(Member member) {
        return authTokenService.genRefreshToken(member);
    }

    public boolean isValidToken(String token) {
        return authTokenService.isValidToken(token);
    }

    public boolean isRefreshToken(String token) {
        return authTokenService.isRefreshToken(token);
    }
}
