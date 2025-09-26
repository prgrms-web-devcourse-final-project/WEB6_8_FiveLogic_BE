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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberAuthControllerTest {
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
                                            "nickname": "유저1",
                                            "interestedField": "Backend"
                                        }
                                        """.stripIndent())

                )
                .andDo(print());
        Member member = memberService.findByEmail("user1@example.com").get();

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
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
                .andExpect(handler().handlerType(MemberAuthController.class))
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
                                            "nickname": "멘토닉네임",
                                            "career": "Backend",
                                            "careerYears": 5
                                        }
                                        """, email, verificationCode).stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail(email).get();

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
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
                                            "nickname": "잘못된닉네임",
                                            "career": "Backend",
                                            "careerYears": 5
                                        }
                                        """, email).stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("인증번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("멘티 회원가입 시 이메일 중복 오류")
    void t3() throws Exception {
        // 기존 멘티 회원가입
        memberService.joinMentee("duplicate@example.com", "기존사용자", "기존닉네임", "password123", "Backend");

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
                                            "nickname": "새닉네임",
                                            "interestedField": "Frontend"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
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
        memberService.joinMentor(email, "기존멘토", "기존멘토닉네임", "password123", "Backend", 5);

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
                                            "nickname": "새멘토닉네임",
                                            "career": "Frontend",
                                            "careerYears": 3
                                        }
                                        """, email, verificationCode).stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
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
        memberService.joinMentee(email, "멘티사용자", "멘티닉네임", "password123", "Backend");

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

    @Test
    @DisplayName("멘티 로그인 성공 - 쿠키에 토큰 저장 확인")
    void t6() throws Exception {
        // 멘티 회원가입
        String email = "login@example.com";
        String password = "password123";
        memberService.joinMentee(email, "로그인사용자", "로그인닉네임", password, "Backend");

        // 로그인 요청
        ResultActions result = mvc
                .perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("""
                                        {
                                            "email": "%s",
                                            "password": "%s"
                                        }
                                        """, email, password))
                )
                .andDo(print());

        result
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-4"))
                .andExpect(jsonPath("$.msg").value("로그인 성공"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void t7() throws Exception {
        // 멘티 회원가입
        String email = "fail@example.com";
        memberService.joinMentee(email, "실패사용자", "실패닉네임", "password123", "Backend");

        // 잘못된 비밀번호로 로그인 시도
        ResultActions result = mvc
                .perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("""
                                        {
                                            "email": "%s",
                                            "password": "wrongpassword"
                                        }
                                        """, email))
                )
                .andDo(print());

        result
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 실패")
    void t8() throws Exception {
        // 존재하지 않는 이메일로 로그인 시도
        ResultActions result = mvc
                .perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "notexist@example.com",
                                            "password": "password123"
                                        }
                                        """)
                )
                .andDo(print());

        result
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 성공 - 쿠키 삭제 확인")
    void t9() throws Exception {
        // 멘티 회원가입 및 로그인
        String email = "logout@example.com";
        memberService.joinMentee(email, "로그아웃사용자", "로그아웃닉네임", "password123", "Backend");

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

        Cookie accessToken = loginResult.andReturn().getResponse().getCookie("accessToken");
        Cookie refreshToken = loginResult.andReturn().getResponse().getCookie("refreshToken");

        // 로그아웃 요청
        ResultActions result = mvc
                .perform(
                        post("/auth/logout")
                                .cookie(accessToken)
                                .cookie(refreshToken)
                )
                .andDo(print());

        result
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-8"))
                .andExpect(jsonPath("$.msg").value("로그아웃 성공"))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    @DisplayName("멘티 회원가입 시 닉네임 중복 오류")
    void t10() throws Exception {
        // 기존 멘티 회원가입
        memberService.joinMentee("existing@example.com", "기존사용자", "중복닉네임", "password123", "Backend");

        // 동일한 닉네임으로 다시 회원가입 시도
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "new@example.com",
                                            "password": "password456",
                                            "name": "새사용자",
                                            "nickname": "중복닉네임",
                                            "interestedField": "Frontend"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
                .andExpect(handler().methodName("signupMentee"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-3"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 닉네임입니다."));
    }

    @Test
    @DisplayName("멘토 회원가입 시 닉네임 중복 오류")
    void t11() throws Exception {
        String email = "newmentor@example.com";

        // 기존 멘토 회원가입
        memberService.joinMentor("existing@example.com", "기존멘토", "중복멘토닉네임", "password123", "Backend", 5);

        // 인증번호 생성
        String verificationCode = emailVerificationService.generateAndSendCode(email);

        // 동일한 닉네임으로 다시 회원가입 시도
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
                                            "nickname": "중복멘토닉네임",
                                            "career": "Frontend",
                                            "careerYears": 3
                                        }
                                        """, email, verificationCode).stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberAuthController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-4"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 닉네임입니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void t12() throws Exception {
        // 멘티 회원가입
        String email = "delete@example.com";
        memberService.joinMentee(email, "탈퇴사용자", "탈퇴닉네임", "password123", "Backend");

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

        Cookie accessToken = loginResult.andReturn().getResponse().getCookie("accessToken");

        // 회원 탈퇴 요청
        ResultActions result = mvc
                .perform(
                        post("/auth/me/withdraw")
                                .cookie(accessToken)
                )
                .andDo(print());

        result
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-7"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        // 소프트 삭제이므로 일반 조회에서는 없어야 함
        assertThat(memberService.findByEmail(email)).isEmpty();

        // 하지만 삭제 포함 조회에서는 존재해야 함 (삭제 상태로)
        // memberRepository.findByEmailIncludingDeleted(email)로 조회하면 존재해야 함
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 회원 탈퇴 시도 - 실패")
    void t13() throws Exception {
        // 로그인 없이 회원 탈퇴 시도
        ResultActions result = mvc
                .perform(post("/auth/me/withdraw"))
                .andDo(print());

        result
                .andExpect(status().isUnauthorized());
    }

    // TODO: 마이페이지 관련 테스트들은 MemberMyPageControllerTest로 이동됨

}
