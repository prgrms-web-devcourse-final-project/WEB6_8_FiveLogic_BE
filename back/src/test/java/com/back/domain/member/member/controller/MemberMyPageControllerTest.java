package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class MemberMyPageControllerTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("멘티 마이페이지 조회 성공")
    void t1() throws Exception {
        String email = "mentee@example.com";
        memberService.joinMentee(email, "멘티유저", "멘티닉네임", "password123", "Backend");

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

        ResultActions result = mvc
                .perform(
                        get("/members/me/mentee")
                                .cookie(accessToken)
                )
                .andDo(print());

        result
                .andExpect(handler().handlerType(MemberMyPageController.class))
                .andExpect(handler().methodName("getMenteeMyPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-9"))
                .andExpect(jsonPath("$.msg").value("멘티 정보 조회 성공"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.name").value("멘티유저"))
                .andExpect(jsonPath("$.data.nickname").value("멘티닉네임"));
    }

    @Test
    @DisplayName("멘티 정보 수정 성공")
    void t2() throws Exception {
        // 멘티 회원가입
        String email = "mentee2@example.com";
        memberService.joinMentee(email, "멘티유저2", "멘티닉네임2", "password123", "Frontend");

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

        // 멘티 정보 수정
        ResultActions result = mvc
                .perform(
                        put("/members/me/mentee")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nickname": "새로운닉네임",
                                            "interestedField": "Mobile"
                                        }
                                        """)
                )
                .andDo(print());

        result
                .andExpect(handler().handlerType(MemberMyPageController.class))
                .andExpect(handler().methodName("updateMentee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-10"))
                .andExpect(jsonPath("$.msg").value("멘티 정보 수정 성공"));
    }

    @Test
    @DisplayName("멘토 마이페이지 조회 성공")
    void t3() throws Exception {
        // 멘토 회원가입
        String email = "mentor@example.com";
        memberService.joinMentor(email, "멘토유저", "멘토닉네임", "password123", "Backend", 5);

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

        // 멘토 마이페이지 조회
        ResultActions result = mvc
                .perform(
                        get("/members/me/mentor")
                                .cookie(accessToken)
                )
                .andDo(print());

        result
                .andExpect(handler().handlerType(MemberMyPageController.class))
                .andExpect(handler().methodName("getMentorMyPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-11"))
                .andExpect(jsonPath("$.msg").value("멘토 정보 조회 성공"))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.name").value("멘토유저"))
                .andExpect(jsonPath("$.data.nickname").value("멘토닉네임"))
                .andExpect(jsonPath("$.data.careerYears").value(5));
    }

    @Test
    @DisplayName("멘토 정보 수정 성공")
    void t4() throws Exception {
        // 멘토 회원가입
        String email = "mentor2@example.com";
        memberService.joinMentor(email, "멘토유저2", "멘토닉네임2", "password123", "Frontend", 3);

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

        // 멘토 정보 수정
        ResultActions result = mvc
                .perform(
                        put("/members/me/mentor")
                                .cookie(accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nickname": "새로운멘토닉네임",
                                            "career": "Fullstack",
                                            "careerYears": 7
                                        }
                                        """)
                )
                .andDo(print());

        result
                .andExpect(handler().handlerType(MemberMyPageController.class))
                .andExpect(handler().methodName("updateMentor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-12"))
                .andExpect(jsonPath("$.msg").value("멘토 정보 수정 성공"));
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 멘티 마이페이지 접근 - 실패")
    void t5() throws Exception {
        ResultActions result = mvc
                .perform(get("/members/me/mentee"))
                .andDo(print());

        result
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 멘토 마이페이지 접근 - 실패")
    void t6() throws Exception {
        ResultActions result = mvc
                .perform(get("/members/me/mentor"))
                .andDo(print());

        result
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 멘티 정보 수정 - 실패")
    void t7() throws Exception {
        ResultActions result = mvc
                .perform(
                        put("/members/me/mentee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nickname": "새로운닉네임",
                                            "interestedField": "Mobile"
                                        }
                                        """)
                )
                .andDo(print());

        result
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 멘토 정보 수정 - 실패")
    void t8() throws Exception {
        ResultActions result = mvc
                .perform(
                        put("/members/me/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nickname": "새로운멘토닉네임",
                                            "career": "Fullstack",
                                            "careerYears": 7
                                        }
                                        """)
                )
                .andDo(print());

        result
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}