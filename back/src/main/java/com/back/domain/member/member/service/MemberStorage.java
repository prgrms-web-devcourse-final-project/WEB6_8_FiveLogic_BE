package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.error.MemberErrorCode;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberStorage {
    private final MemberRepository memberRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;

    // ===== find 메서드 =====

    public Mentor findMentorByMember(Member member) {
        return findMentorByMemberId(member.getId());
    }

    public Mentor findMentorByMemberId(Long memberId) {
        return mentorRepository.findByMemberIdWithMember(memberId)
            .orElseThrow(() -> new ServiceException(MemberErrorCode.NOT_FOUND_MENTOR));
    }

    public Mentee findMenteeByMember(Member member) {
        return menteeRepository.findByMemberIdWithMember(member.getId())
            .orElseThrow(() -> new ServiceException(MemberErrorCode.NOT_FOUND_MENTEE));
    }


    // ==== exists 메서드 =====

    public boolean existsMentorById(Long mentorId) {
        return mentorRepository.existsById(mentorId);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new ServiceException(MemberErrorCode.NOT_FOUND_MEMBER));
    }
}
