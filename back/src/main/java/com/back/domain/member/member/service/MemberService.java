package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;

    @Transactional
    public Member join(String email, String name, String password, Member.Role role) {
        memberRepository.findByEmail(email).ifPresent(
                member -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                }
        );

        Member member = new Member(email, password, name, role);
        Member savedMember = memberRepository.save(member);

        // 역할에 따라 해당 테이블에 추가
        switch (role) {
            case MENTOR -> {
                Mentor mentor = new Mentor(savedMember, null, null, null);
                mentorRepository.save(mentor);
            }
            case MENTEE -> {
                Mentee mentee = new Mentee(savedMember, null);
                menteeRepository.save(mentee);
            }
            case ADMIN -> {
                // 관리자는 별도 테이블 없음
            }
        }

        return savedMember;
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
