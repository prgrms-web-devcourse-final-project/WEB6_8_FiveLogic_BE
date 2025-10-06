package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentee.repository.MenteeRepository;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.dto.response.MentoringResponse;
import com.back.domain.mentoring.mentoring.service.MentoringService;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.service.ReservationService;
import com.back.domain.mentoring.slot.dto.request.MentorSlotRequest;
import com.back.domain.mentoring.slot.dto.response.MentorSlotResponse;
import com.back.domain.mentoring.slot.service.MentorSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;

//TODO : 삭제 예정
@Configuration
@RequiredArgsConstructor
public class SessionInitData {
    private final MemberService memberService;
    private final MentoringService mentoringService;
    private final MentorSlotService mentorSlotService;
    private final ReservationService reservationService;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 멘토, 멘티 생성
            Member mentorMember = memberService.joinMentor("mentor@example.com", "Mentor Name", "mentor123", "password123", "IT", 10);
            Member menteeMember = memberService.joinMentee("mentee@example.com", "Mentee Name", "mentee123", "password123", "IT");

            Mentor mentor = mentorRepository.findByMemberIdWithMember(mentorMember.getId()).orElseThrow();
            Mentee mentee = menteeRepository.findByMemberIdWithMember(menteeMember.getId()).orElseThrow();

            // 멘토링 생성
            MentoringRequest mentoringRequest = new MentoringRequest("Test Mentoring", Arrays.asList("Java", "Spring"), "This is a test mentoring.", null);
            MentoringResponse mentoringResponse = mentoringService.createMentoring(mentoringRequest, mentor);

            // 멘토 슬롯 생성
            MentorSlotRequest mentorSlotRequest = new MentorSlotRequest(mentor.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
            MentorSlotResponse mentorSlotResponse = mentorSlotService.createMentorSlot(mentorSlotRequest, mentor);

            // 예약 생성
            ReservationRequest reservationRequest = new ReservationRequest(mentor.getId(), mentorSlotResponse.mentorSlot().mentorSlotId(), mentoringResponse.mentoring().mentoringId(), "Any pre-questions?");
            var reservationResponse = reservationService.createReservation(mentee, reservationRequest);

            // 예약 승인
            reservationService.approveReservation(mentor, reservationResponse.reservation().reservationId());
        };
    }
}