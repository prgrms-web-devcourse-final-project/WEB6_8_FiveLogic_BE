package com.back.domain.member.member.service;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.repository.JobRepository;
import com.back.domain.member.member.dto.*;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final JobRepository jobRepository;

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

        // interestedField로 Job 찾기 또는 생성
        Job job = jobRepository.findByName(interestedField)
                .orElseGet(() -> jobRepository.save(new Job(interestedField, null)));

        Mentee mentee = new Mentee(savedMember, job);
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

        // career로 Job 찾기 또는 생성
        Job job = jobRepository.findByName(career)
                .orElseGet(() -> jobRepository.save(new Job(career, null)));

        Mentor mentor = new Mentor(savedMember, job, null, careerYears);
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

    public MemberMeResponse getMemberMe(Member actor) {
        if (actor == null) {
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }

        Long mentorId = null;
        Long menteeId = null;
        String job = null;

        if (actor.getRole() == Member.Role.MENTOR) {
            Mentor mentor = mentorRepository.findByMemberIdWithMember(actor.getId()).orElse(null);
            if (mentor != null) {
                mentorId = mentor.getId();
                job = mentor.getJob() != null ? mentor.getJob().getName() : null;
            }
        } else if (actor.getRole() == Member.Role.MENTEE) {
            Mentee mentee = menteeRepository.findByMemberId(actor.getId()).orElse(null);
            if (mentee != null) {
                menteeId = mentee.getId();
                job = mentee.getJob() != null ? mentee.getJob().getName() : null;
            }
        }

        return MemberMeResponse.of(actor, mentorId, menteeId, job);
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
        Mentee mentee = menteeRepository.findByMemberIdWithMember(currentUser.getId())
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

        // Mentee 정보 업데이트 (interestedField)
        if (request.interestedField() != null) {
            Mentee mentee = menteeRepository.findByMemberIdWithMember(currentUser.getId())
                    .orElseThrow(() -> new ServiceException("404-2", "멘티 정보를 찾을 수 없습니다."));

            Job job = jobRepository.findByName(request.interestedField())
                    .orElseGet(() -> jobRepository.save(new Job(request.interestedField(), null)));

            mentee.updateJob(job);
            menteeRepository.save(mentee);
        }
    }

    public MentorMyPageResponse getMentorMyPage(Member currentUser) {
        Mentor mentor = mentorRepository.findByMemberIdWithMember(currentUser.getId())
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

        // Mentor 정보 업데이트 (경력연수, career)
        if (request.careerYears() != null) {
            mentor.updateCareerYears(request.careerYears());
        }

        if (request.career() != null) {
            Job job = jobRepository.findByName(request.career())
                    .orElseGet(() -> jobRepository.save(new Job(request.career(), null)));
            mentor.updateJob(job);
        }

        mentorRepository.save(mentor);
    }


    public MemberPagingResponse getAllMembersForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Member> members = memberRepository.findAllIncludingDeleted(pageable);

        Page<MemberListResponse> memberPage = members.map(MemberListResponse::from);
        return MemberPagingResponse.from(memberPage);
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
                if (careerYears != null) {
                    mentor.updateCareerYears(careerYears);
                }
                if (career != null) {
                    Job job = jobRepository.findByName(career)
                            .orElseGet(() -> jobRepository.save(new Job(career, null)));
                    mentor.updateJob(job);
                }
                mentorRepository.save(mentor);
            }
        } else if (member.getRole() == Member.Role.MENTEE && interestedField != null) {
            Mentee mentee = menteeRepository.findByMemberIdIncludingDeleted(member.getId())
                    .orElse(null);
            if (mentee != null) {
                Job job = jobRepository.findByName(interestedField)
                        .orElseGet(() -> jobRepository.save(new Job(interestedField, null)));
                mentee.updateJob(job);
                menteeRepository.save(mentee);
            }
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

    public MentorPagingResponse getAllMentors(int page, int size) {
        List<Mentor> allMentors = mentorRepository.findAllActiveWithMemberAndJob();

        int start = page * size;
        int end = Math.min(start + size, allMentors.size());
        List<Mentor> pagedMentors = allMentors.subList(start, end);

        Page<Mentor> mentorPage = new org.springframework.data.domain.PageImpl<>(
                pagedMentors,
                PageRequest.of(page, size),
                allMentors.size()
        );

        Page<MentorListResponse> responsePage = mentorPage.map(MentorListResponse::from);
        return MentorPagingResponse.from(responsePage);
    }

}
