package com.back.fixture;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.standard.util.Ut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MemberTestFixture {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired private MentorRepository mentorRepository;
    @Autowired private MenteeRepository menteeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpiration;

    private int counter = 0;

    // ===== Member =====

    public Member createMember(String email, String name, Member.Role role) {
        Member member = new Member(
            email,
            passwordEncoder.encode("password123"),
            name,
            role
        );
        return memberRepository.save(member);
    }

    public Member createMentorMember(String email, String name) {
        return createMember(email, name, Member.Role.MENTOR);
    }

    public Member createMentorMember() {
        return createMentorMember("mentor" + (++counter) + "@test.com", "멘토" + counter);
    }

    public Member createMenteeMember(String email, String name) {
        return createMember(email, name, Member.Role.MENTEE);
    }

    public Member createMenteeMember() {
        return createMenteeMember("mentee" + (++counter) + "@test.com", "멘티" + counter);
    }


    // ===== Mentor =====

    public Mentor createMentor(Member member, Long jobId, Double rate, Integer careerYears) {
        Mentor mentor = Mentor.builder()
            .member(member)
            .jobId(jobId)
            .rate(rate)
            .careerYears(careerYears)
            .build();
        return mentorRepository.save(mentor);
    }

    public Mentor createMentor(Member member) {
        return createMentor(member, 1L, 4.5, 5);
    }


    // ===== Mentee =====

    public Mentee createMentee(Member member, Long jobId) {
        Mentee mentee = Mentee.builder()
            .member(member)
            .jobId(jobId)
            .build();
        return menteeRepository.save(mentee);
    }

    public Mentee createMentee(Member member) {
        return createMentee(member, 1L);
    }


    // ===== Token =====

    public String getAccessToken(Member member) {
        return Ut.jwt.toString(jwtSecretKey, accessTokenExpiration, Map.of(
            "id", member.getId(),
            "email", member.getEmail(),
            "name", member.getName(),
            "role", member.getRole().name()
        ));
    }
}