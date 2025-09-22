package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member joinMentee(String email, String name, String password, String interestedField) {
        memberRepository.findByEmail(email).ifPresent(
                member -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                }
        );

        Member member = new Member(email, passwordEncoder.encode(password), name, Member.Role.MENTEE);
        Member savedMember = memberRepository.save(member);

        // TODO: interestedField를 jobId로 매핑하는 로직 필요
        Mentee mentee = new Mentee(savedMember, null);
        menteeRepository.save(mentee);

        return savedMember;
    }

    @Transactional
    public Member joinMentor(String email, String name, String password, String career, Integer careerYears) {
        memberRepository.findByEmail(email).ifPresent(
                member -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                }
        );

        Member member = new Member(email, passwordEncoder.encode(password), name, Member.Role.MENTOR);
        Member savedMember = memberRepository.save(member);

        // TODO: career를 jobId로 매핑하는 로직 필요
        Mentor mentor = new Mentor(savedMember, null, null, careerYears);
        mentorRepository.save(mentor);

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

    public void checkPassword(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword()))
            throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");
    }
}
