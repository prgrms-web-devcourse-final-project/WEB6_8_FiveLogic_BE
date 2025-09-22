package com.back.domain.member.member.controller;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForBigDecimal;
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
    @DisplayName("멘토 회원가입")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "user2@example.com",
                                            "password": "password123",
                                            "name": "사용자2",
                                            "career": "Backend",
                                            "careerYears": 5
                                        }
                                        """.stripIndent())

                )
                .andDo(print());
        Member member = memberService.findByEmail("user2@example.com").get();

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("멘토 회원가입 성공"));
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
        // 기존 멘토 회원가입
        memberService.joinMentor("duplicate2@example.com", "기존멘토", "password123", "Backend", 5);

        // 동일한 이메일로 다시 회원가입 시도
        ResultActions resultActions = mvc
                .perform(
                        post("/auth/signup/mentor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "duplicate2@example.com",
                                            "password": "password456",
                                            "name": "새멘토",
                                            "career": "Frontend",
                                            "careerYears": 3
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("signupMentor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 이메일입니다."));
    }

}
