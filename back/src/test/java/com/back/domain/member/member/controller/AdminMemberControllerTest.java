package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminMemberControllerTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("관리자 회원 상세 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void t1() throws Exception {
        // 멘티 회원가입
        String email = "mentee@example.com";
        Member mentee = memberService.joinMentee(email, "멘티유저", "멘티닉네임", "password123", "Backend");

        // 관리자 권한으로 회원 정보 조회
        ResultActions result = mvc
                .perform(get("/members/" + mentee.getId()))
                .andDo(print());

        result
                .andExpect(handler().handlerType(AdmMemberController.class))
                .andExpect(handler().methodName("getMember"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-14"))
                .andExpect(jsonPath("$.msg").value("회원 상세 조회 성공"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.role").value("MENTEE"));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자의 회원 정보 조회 - 실패")
    @WithMockUser(roles = "MENTEE")
    void t2() throws Exception {
        // 테스트용 회원 생성
        String email = "test@example.com";
        Member testMember = memberService.joinMentee(email, "테스트유저", "테스트닉네임", "password123", "Backend");

        // 일반 사용자(MENTEE 권한)가 관리자 기능 접근 시도
        ResultActions result = mvc
                .perform(get("/members/" + testMember.getId()))
                .andDo(print());

        result.andExpect(status().isForbidden()); // 403 Forbidden 예상
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 관리자 기능 접근 - 실패")
    void t3() throws Exception {
        Long memberId = 1L;

        ResultActions result = mvc
                .perform(get("/members/" + memberId))
                .andDo(print());

        result
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("멘티 검색 시 희망직업 정보 확인")
    @WithMockUser(roles = "ADMIN")
    void t4() throws Exception {
        // 멘티 회원가입
        String email = "mentee@test.com";
        Member mentee = memberService.joinMentee(email, "멘티유저", "멘티닉네임", "password123", "Backend");

        // 관리자가 멘티 정보 조회
        ResultActions result = mvc
                .perform(get("/members/" + mentee.getId()))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MENTEE"))
                .andExpect(jsonPath("$.data.interestedField").exists()) // 희망직업 필드 존재 확인
                .andExpect(jsonPath("$.data.careerYears").doesNotExist()); // 멘티는 연차 없음
    }

    @Test
    @DisplayName("멘토 검색 시 직업과 연차 정보 확인")
    @WithMockUser(roles = "ADMIN")
    void t5() throws Exception {
        // 멘토 회원가입
        String email = "mentor@test.com";
        Member mentor = memberService.joinMentor(email, "멘토유저", "멘토닉네임", "password123", "Backend", 5);

        // 관리자가 멘토 정보 조회
        ResultActions result = mvc
                .perform(get("/members/" + mentor.getId()))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MENTOR"))
                .andExpect(jsonPath("$.data.career").exists()) // 직업 필드 존재 확인
                .andExpect(jsonPath("$.data.careerYears").value(5)) // 연차 확인
                .andExpect(jsonPath("$.data.interestedField").doesNotExist()); // 멘토는 희망직업 없음
    }
}