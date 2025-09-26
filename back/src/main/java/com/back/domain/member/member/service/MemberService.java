package com.back.domain.member.member.service;

import com.back.domain.member.member.dto.*;
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
    public Member joinMentee(String email, String name, String nickname, String password, String interestedField) {
        // 활성 사용자 중 이메일 중복 체크 (탈퇴한 사용자 제외)
        memberRepository.findByEmail(email).ifPresent(
                member -> {
                    throw new ServiceException("400-1", "이미 존재하는 이메일입니다.");
                }
        );

        // 활성 사용자 중 닉네임 중복 체크 (탈퇴한 사용자 제외)
        memberRepository.findByNickname(nickname).ifPresent(
                member -> {
                    throw new ServiceException("400-3", "이미 존재하는 닉네임입니다.");
                }
        );

        Member member = new Member(email, passwordEncoder.encode(password), name, nickname, Member.Role.MENTEE);
        Member savedMember = memberRepository.save(member);

        // TODO: interestedField를 jobId로 매핑하는 로직 필요
        Mentee mentee = new Mentee(savedMember, null);
        menteeRepository.save(mentee);

        return savedMember;
    }

    @Transactional
    public Member joinMentor(String email, String name, String nickname, String password, String career, Integer careerYears) {
        // 활성 사용자 중 이메일 중복 체크 (탈퇴한 사용자 제외)
        memberRepository.findByEmail(email).ifPresent(
                member -> {
                    throw new ServiceException("400-2", "이미 존재하는 이메일입니다.");
                }
        );

        // 활성 사용자 중 닉네임 중복 체크 (탈퇴한 사용자 제외)
        memberRepository.findByNickname(nickname).ifPresent(
                member -> {
                    throw new ServiceException("400-4", "이미 존재하는 닉네임입니다.");
                }
        );

        Member member = new Member(email, passwordEncoder.encode(password), name, nickname, Member.Role.MENTOR);
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

    @Transactional
    public void deleteMember(Member currentUser) {
        if (currentUser == null) {
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }

        Member member = memberRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 회원입니다."));

        // 소프트 삭제 처리
        member.delete();
        memberRepository.save(member);

        // 관련 엔티티들도 소프트 삭제
        menteeRepository.findByMemberIdIncludingDeleted(member.getId()).ifPresent(mentee -> {
            mentee.delete();
            menteeRepository.save(mentee);
        });

        mentorRepository.findByMemberIdIncludingDeleted(member.getId()).ifPresent(mentor -> {
            mentor.delete();
            mentorRepository.save(mentor);
        });
    }

    public boolean isRefreshToken(String token) {
        return authTokenService.isRefreshToken(token);
    }

    public void checkPassword(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword()))
            throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");
    }

    public Member login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("400-3", "존재하지 않는 이메일입니다."));

        checkPassword(member, password);
        return member;
    }

    public Member getCurrentUser(Member actor) {
        if (actor == null) {
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }
        return actor;
    }

    public Member refreshAccessToken(String refreshToken) {
        if (refreshToken.isBlank()) {
            throw new ServiceException("401-1", "Refresh token이 없습니다.");
        }

        // Refresh token 유효성 검증
        if (!isValidToken(refreshToken)) {
            throw new ServiceException("401-2", "유효하지 않은 refresh token입니다.");
        }

        // Refresh token인지 확인
        if (!isRefreshToken(refreshToken)) {
            throw new ServiceException("401-3", "Access token으로는 갱신할 수 없습니다.");
        }

        // Refresh token에서 사용자 정보 추출
        Map<String, Object> payload = payload(refreshToken);
        if (payload == null) {
            throw new ServiceException("401-4", "토큰에서 사용자 정보를 추출할 수 없습니다.");
        }

        String email = (String) payload.get("email");
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("401-5", "존재하지 않는 사용자입니다."));
    }

    public MenteeMyPageResponse getMenteeMyPage(Member currentUser) {
        Mentee mentee = menteeRepository.findByMemberId(currentUser.getId())
                .orElseThrow(() -> new ServiceException("404-2", "멘티 정보를 찾을 수 없습니다."));

        return MenteeMyPageResponse.from(currentUser, mentee);
    }

    @Transactional
    public void updateMentee(Member currentUser, MenteeUpdateRequest request) {
        // 닉네임 중복 체크
        validateNicknameDuplicate(request.nickname(), currentUser);

        // Member 정보 업데이트 (닉네임)
        Member member = memberRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 회원입니다."));

        member.updateNickname(request.nickname());
        memberRepository.save(member);

        // TODO: interestedField를 jobId로 매핑하는 로직 필요 (현재는 기존 jobId 유지)
    }

    public MentorMyPageResponse getMentorMyPage(Member currentUser) {
        Mentor mentor = mentorRepository.findByMemberId(currentUser.getId())
                .orElseThrow(() -> new ServiceException("404-3", "멘토 정보를 찾을 수 없습니다."));

        return MentorMyPageResponse.from(currentUser, mentor);
    }

    @Transactional
    public void updateMentor(Member currentUser, MentorUpdateRequest request) {
        // 닉네임 중복 체크
        validateNicknameDuplicate(request.nickname(), currentUser);

        Mentor mentor = mentorRepository.findByMemberId(currentUser.getId())
                .orElseThrow(() -> new ServiceException("404-3", "멘토 정보를 찾을 수 없습니다."));

        // Member 정보 업데이트 (닉네임)
        Member member = memberRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 회원입니다."));

        member.updateNickname(request.nickname());
        memberRepository.save(member);

        // Mentor 정보 업데이트 (경력연수)
        mentor.updateCareerYears(request.careerYears());
        mentorRepository.save(mentor);

        // TODO: career를 jobId로 매핑하는 로직 필요 (현재는 기존 jobId 유지)
    }


    public MemberSearchResponse getMemberForAdmin(Long memberId) {
        Member member = memberRepository.findByIdIncludingDeleted(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 회원입니다."));

        Mentor mentor = null;
        Mentee mentee = null;

        if (member.getRole() == Member.Role.MENTOR) {
            mentor = mentorRepository.findByMemberIdIncludingDeleted(member.getId()).orElse(null);
        } else if (member.getRole() == Member.Role.MENTEE) {
            mentee = menteeRepository.findByMemberIdIncludingDeleted(member.getId()).orElse(null);
        }

        return MemberSearchResponse.from(member, mentor, mentee);
    }

    @Transactional
    public void deleteMemberByAdmin(Long memberId) {
        Member member = memberRepository.findByIdIncludingDeleted(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 회원입니다."));

        // 이미 탈퇴한 회원인지 확인
        if (member.getIsDeleted()) {
            throw new ServiceException("400-1", "이미 탈퇴한 회원입니다.");
        }

        // 소프트 삭제 처리
        member.delete();
        memberRepository.save(member);

        // 관련 엔티티들도 소프트 삭제
        menteeRepository.findByMemberIdIncludingDeleted(member.getId()).ifPresent(mentee -> {
            mentee.delete();
            menteeRepository.save(mentee);
        });

        mentorRepository.findByMemberIdIncludingDeleted(member.getId()).ifPresent(mentor -> {
            mentor.delete();
            mentorRepository.save(mentor);
        });
    }

    @Transactional
    public void updateMemberByAdmin(Long memberId, String name, String nickname, String email,
                                   String career, Integer careerYears, String interestedField) {
        Member member = memberRepository.findByIdIncludingDeleted(memberId)
                .orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 회원입니다."));

        // 중복 체크
        validateEmailDuplicate(email, member);
        validateNicknameDuplicate(nickname, member);

        // Member 정보 업데이트
        if (name != null) member.updateName(name);
        if (nickname != null) member.updateNickname(nickname);
        if (email != null) member.updateEmail(email);

        memberRepository.save(member);

        // 역할별 추가 정보 업데이트
        if (member.getRole() == Member.Role.MENTOR && (career != null || careerYears != null)) {
            Mentor mentor = mentorRepository.findByMemberIdIncludingDeleted(member.getId())
                    .orElse(null);
            if (mentor != null) {
                if (careerYears != null) mentor.updateCareerYears(careerYears);
                mentorRepository.save(mentor);
            }
        } else if (member.getRole() == Member.Role.MENTEE && interestedField != null) {
            // TODO: interestedField 업데이트 로직 필요 (Mentee 엔티티에 업데이트 메서드가 있을 때)
        }
    }

    private void validateEmailDuplicate(String email, Member currentMember) {
        if (email == null || email.equals(currentMember.getEmail())) return;

        if (memberRepository.findByEmail(email).isPresent()) {
            throw new ServiceException("400-1", "이미 존재하는 이메일입니다.");
        }
    }

    private void validateNicknameDuplicate(String nickname, Member currentMember) {
        if (nickname == null || nickname.equals(currentMember.getNickname())) return;

        if (memberRepository.findByNickname(nickname).isPresent()) {
            throw new ServiceException("400-3", "이미 존재하는 닉네임입니다.");
        }
    }

}
