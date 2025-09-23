package com.back.domain.member.member.controller;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.member.verification.EmailVerificationService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class MemberControllerTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    @DisplayName("멘티 회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "user1@example.com",
                                            "password": "password123",
                                            "name": "사용자1",
                                            "interestedField": "Backend"
                                        }
                                        """.stripIndent())

                )
                .andDo(print());
        Member member = memberService.findByEmail("user1@example.com").get();

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentee"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("멘티 회원가입 성공"));
    }

    @Test
    @DisplayName("멘토 인증번호 발송")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentor/send-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "mentor@example.com"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("sendMentorVerification"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-2"))
                .andExpect(jsonPath("$.msg").value("인증번호가 발송되었습니다."));
    }

    @Test
    @DisplayName("멘토 회원가입 (인증번호 확인)")
    void t2_1() throws Exception {
        String email = "mentor2@example.com";

        // 인증번호 생성
        String verificationCode = emailVerificationService.generateAndSendCode(email);

        // 회원가입 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("""
                                        {
                                            "email": "%s",
                                            "verificationCode": "%s",
                                            "password": "password123",
                                            "name": "멘토사용자",
                                            "career": "Backend",
                                            "careerYears": 5
                                        }
                                        """, email, verificationCode).stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail(email).get();

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("멘토 회원가입 성공"));
    }

    @Test
    @DisplayName("멘토 회원가입 시 잘못된 인증번호")
    void t2_2() throws Exception {
        String email = "mentor3@example.com";

        // 인증번호 생성
        emailVerificationService.generateAndSendCode(email);

        // 잘못된 인증번호로 회원가입 시도
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("""
                                        {
                                            "email": "%s",
                                            "verificationCode": "999999",
                                            "password": "password123",
                                            "name": "멘토사용자",
                                            "career": "Backend",
                                            "careerYears": 5
                                        }
                                        """, email).stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("인증번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("멘티 회원가입 시 이메일 중복 오류")
    void t3() throws Exception {
        // 기존 멘티 회원가입
        memberService.joinMentee("duplicate@example.com", "기존사용자", "password123", "Backend");

        // 동일한 이메일로 다시 회원가입 시도
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "duplicate@example.com",
                                            "password": "password456",
                                            "name": "새사용자",
                                            "interestedField": "Frontend"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentee"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("멘토 회원가입 시 이메일 중복 오류")
    void t4() throws Exception {
        String email = "duplicate2@example.com";

        // 기존 멘토 회원가입
        memberService.joinMentor(email, "기존멘토", "password123", "Backend", 5);

        // 인증번호 생성
        String verificationCode = emailVerificationService.generateAndSendCode(email);

        // 동일한 이메일로 다시 회원가입 시도
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("""
                                        {
                                            "email": "%s",
                                            "verificationCode": "%s",
                                            "password": "password456",
                                            "name": "새멘토",
                                            "career": "Frontend",
                                            "careerYears": 3
                                        }
                                        """, email, verificationCode).stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("멘티 로그인 후 /auth/me로 정보 조회 - rq.getActor() role 확인")
    void t5() throws Exception {
        // 멘티 회원가입
        String email = "mentee@example.com";
        memberService.joinMentee(email, "멘티사용자", "password123", "Backend");

        // 로그인하여 쿠키 받기
        ResultActions loginResult = mvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "email": "%s",
                                    "password": "password123"
                                }
                                """, email))
        );

        // 로그인 응답에서 쿠키 추출
        Cookie accessToken = loginResult.andReturn().getResponse().getCookie("accessToken");

        // /auth/me 호출하여 role 확인 (쿠키 포함)
        ResultActions result = mvc
                .perform(get("/auth/me")
                        .cookie(accessToken))
                .andDo(print());

        result
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.role").value("MENTEE"));
    }

}
