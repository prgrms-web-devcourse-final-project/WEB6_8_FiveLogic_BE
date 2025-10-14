package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MenteeUpdateRequest;
import com.back.domain.member.member.dto.MentorUpdateRequest;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    @Autowired
    private ObjectMapper objectMapper;

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
                .andExpect(jsonPath("$.data.job").value("Backend")) // 희망직업 확인
                .andExpect(jsonPath("$.data.careerYears").doesNotExist()) // 멘티는 연차 없음
                .andExpect(jsonPath("$.data.menteeId").exists()) // 멘티 ID 존재 확인
                .andExpect(jsonPath("$.data.mentorId").doesNotExist()); // 멘토 ID는 없음
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
                .andExpect(jsonPath("$.data.job").value("Backend")) // 직업 확인
                .andExpect(jsonPath("$.data.careerYears").value(5)) // 연차 확인
                .andExpect(jsonPath("$.data.mentorId").exists()) // 멘토 ID 존재 확인
                .andExpect(jsonPath("$.data.menteeId").doesNotExist()); // 멘티 ID는 없음
    }

    @Test
    @DisplayName("관리자 회원 삭제 성공")
    @WithMockUser(roles = "ADMIN")
    void t6() throws Exception {
        // 테스트용 멘티 생성
        String email = "test@delete.com";
        Member member = memberService.joinMentee(email, "삭제될유저", "삭제될닉네임", "password123", "Backend");

        // 관리자가 회원 삭제
        ResultActions result = mvc
                .perform(post("/members/" + member.getId() + "/delete"))
                .andDo(print());

        result
                .andExpect(handler().handlerType(AdmMemberController.class))
                .andExpect(handler().methodName("deleteMember"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-15"))
                .andExpect(jsonPath("$.msg").value("회원 삭제 성공"));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자의 회원 삭제 시도 - 실패")
    @WithMockUser(roles = "MENTEE")
    void t7() throws Exception {
        // 테스트용 멘티 생성
        String email = "test@fail.com";
        Member member = memberService.joinMentee(email, "테스트유저", "테스트닉네임", "password123", "Backend");

        // 일반 사용자가 회원 삭제 시도
        ResultActions result = mvc
                .perform(post("/members/" + member.getId() + "/delete"))
                .andDo(print());

        result.andExpect(status().isForbidden()); // 403 Forbidden 예상
    }

    @Test
    @DisplayName("관리자 멘토 정보 수정 성공")
    @WithMockUser(roles = "ADMIN")
    void t8() throws Exception {
        // 테스트용 멘토 생성
        String email = "mentor@update.com";
        Member mentor = memberService.joinMentor(email, "멘토유저", "멘토닉네임", "password123", "Backend", 3);

        // 수정할 데이터
        MentorUpdateRequest updateRequest = new MentorUpdateRequest("새로운멘토닉네임", "Frontend", 5);

        // 관리자가 멘토 정보 수정
        ResultActions result = mvc
                .perform(put("/members/" + mentor.getId() + "/mentor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print());

        result
                .andExpect(handler().handlerType(AdmMemberController.class))
                .andExpect(handler().methodName("updateMentor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-16"))
                .andExpect(jsonPath("$.msg").value("멘토 정보 수정 성공"));

        // 수정 후 실제로 값이 변경되었는지 확인
        ResultActions verifyResult = mvc
                .perform(get("/members/" + mentor.getId()))
                .andDo(print());

        verifyResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새로운멘토닉네임"))
                .andExpect(jsonPath("$.data.careerYears").value(5));
    }

    @Test
    @DisplayName("관리자 멘티 정보 수정 성공")
    @WithMockUser(roles = "ADMIN")
    void t9() throws Exception {
        // 테스트용 멘티 생성
        String email = "mentee@update.com";
        Member mentee = memberService.joinMentee(email, "멘티유저", "멘티닉네임", "password123", "Backend");

        // 수정할 데이터
        MenteeUpdateRequest updateRequest = new MenteeUpdateRequest("새로운멘티닉네임", "AI/ML");

        // 관리자가 멘티 정보 수정
        ResultActions result = mvc
                .perform(put("/members/" + mentee.getId() + "/mentee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print());

        result
                .andExpect(handler().handlerType(AdmMemberController.class))
                .andExpect(handler().methodName("updateMentee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-17"))
                .andExpect(jsonPath("$.msg").value("멘티 정보 수정 성공"));

        // 수정 후 실제로 값이 변경되었는지 확인
        ResultActions verifyResult = mvc
                .perform(get("/members/" + mentee.getId()))
                .andDo(print());

        verifyResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새로운멘티닉네임"));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자의 멘토 정보 수정 시도 - 실패")
    @WithMockUser(roles = "MENTEE")
    void t10() throws Exception {
        // 테스트용 멘토 생성
        String email = "mentor@fail.com";
        Member mentor = memberService.joinMentor(email, "멘토유저", "멘토닉네임", "password123", "Backend", 3);

        MentorUpdateRequest updateRequest = new MentorUpdateRequest("해킹시도닉네임", "Hacker", 10);

        // 일반 사용자가 멘토 정보 수정 시도
        ResultActions result = mvc
                .perform(put("/members/" + mentor.getId() + "/mentor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print());

        result.andExpect(status().isForbidden()); // 403 Forbidden 예상
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 - 실패")
    @WithMockUser(roles = "ADMIN")
    void t11() throws Exception {
        Long nonExistentId = 999999L;

        ResultActions result = mvc
                .perform(get("/members/" + nonExistentId))
                .andDo(print());

        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("닉네임 중복으로 멘토 수정 실패")
    @WithMockUser(roles = "ADMIN")
    void t12() throws Exception {
        // 첫 번째 멘토 생성
        String email1 = "mentor1@test.com";
        Member mentor1 = memberService.joinMentor(email1, "멘토1", "기존닉네임", "password123", "Backend", 3);

        // 두 번째 멘토 생성
        String email2 = "mentor2@test.com";
        Member mentor2 = memberService.joinMentor(email2, "멘토2", "다른닉네임", "password123", "Frontend", 5);

        // 두 번째 멘토의 닉네임을 첫 번째 멘토의 닉네임으로 변경 시도 (중복)
        MentorUpdateRequest updateRequest = new MentorUpdateRequest("기존닉네임", "AI/ML", 7);

        ResultActions result = mvc
                .perform(put("/members/" + mentor2.getId() + "/mentor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print());

        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("관리자 회원 목록 조회 성공 - 기본 페이징")
    @WithMockUser(roles = "ADMIN")
    void t13() throws Exception {
        // 테스트용 회원 3명 생성
        memberService.joinMentee("mentee1@test.com", "멘티1", "멘티닉네임1", "password123", "Backend");
        memberService.joinMentor("mentor1@test.com", "멘토1", "멘토닉네임1", "password123", "Frontend", 3);
        memberService.joinMentee("mentee2@test.com", "멘티2", "멘티닉네임2", "password123", "AI/ML");

        // 관리자가 회원 목록 조회 (기본값: page=0, size=10)
        ResultActions result = mvc
                .perform(get("/members"))
                .andDo(print());

        result
                .andExpect(handler().handlerType(AdmMemberController.class))
                .andExpect(handler().methodName("getAllMembers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-18"))
                .andExpect(jsonPath("$.msg").value("회원 목록 조회 성공"))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.totalPage").exists())
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.hasNext").exists());
    }

    @Test
    @DisplayName("관리자 회원 목록 조회 성공 - 커스텀 페이징")
    @WithMockUser(roles = "ADMIN")
    void t14() throws Exception {
        // 테스트용 회원 5명 생성
        memberService.joinMentee("user1@test.com", "유저1", "닉네임1", "password123", "Backend");
        memberService.joinMentee("user2@test.com", "유저2", "닉네임2", "password123", "Frontend");
        memberService.joinMentor("user3@test.com", "유저3", "닉네임3", "password123", "Backend", 2);
        memberService.joinMentor("user4@test.com", "유저4", "닉네임4", "password123", "AI/ML", 5);
        memberService.joinMentee("user5@test.com", "유저5", "닉네임5", "password123", "DevOps");

        // 페이지 크기 2로 첫 페이지 조회
        ResultActions result = mvc
                .perform(get("/members")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-18"))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.members.length()").value(2));
    }

    @Test
    @DisplayName("관리자 회원 목록 조회 - 두 번째 페이지")
    @WithMockUser(roles = "ADMIN")
    void t15() throws Exception {
        // 테스트용 회원 5명 생성
        memberService.joinMentee("page2user1@test.com", "페이지유저1", "페이지닉네임1", "password123", "Backend");
        memberService.joinMentee("page2user2@test.com", "페이지유저2", "페이지닉네임2", "password123", "Frontend");
        memberService.joinMentor("page2user3@test.com", "페이지유저3", "페이지닉네임3", "password123", "Backend", 3);
        memberService.joinMentor("page2user4@test.com", "페이지유저4", "페이지닉네임4", "password123", "AI/ML", 4);
        memberService.joinMentee("page2user5@test.com", "페이지유저5", "페이지닉네임5", "password123", "DevOps");

        // 페이지 크기 2로 두 번째 페이지 조회
        ResultActions result = mvc
                .perform(get("/members")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-18"))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(1));
    }

    @Test
    @DisplayName("관리자 회원 목록 조회 - 기본 정보만 포함 확인")
    @WithMockUser(roles = "ADMIN")
    void t16() throws Exception {
        // 멘토와 멘티 생성
        memberService.joinMentor("mentor@list.com", "멘토유저", "멘토닉네임", "password123", "Backend", 5);
        memberService.joinMentee("mentee@list.com", "멘티유저", "멘티닉네임", "password123", "Frontend");

        // 회원 목록 조회
        ResultActions result = mvc
                .perform(get("/members")
                        .param("size", "10"))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.members").isArray())
                // 기본 정보만 포함되어야 함
                .andExpect(jsonPath("$.data.members[0].id").exists())
                .andExpect(jsonPath("$.data.members[0].email").exists())
                .andExpect(jsonPath("$.data.members[0].name").exists())
                .andExpect(jsonPath("$.data.members[0].nickname").exists())
                .andExpect(jsonPath("$.data.members[0].role").exists())
                .andExpect(jsonPath("$.data.members[0].isDeleted").exists())
                .andExpect(jsonPath("$.data.members[0].createdAt").exists());
    }

    @Test
    @DisplayName("관리자 회원 목록 조회 - 삭제된 회원도 포함")
    @WithMockUser(roles = "ADMIN")
    void t17() throws Exception {
        // 회원 생성 후 삭제
        Member member = memberService.joinMentee("deleted@test.com", "삭제될유저", "삭제될닉네임", "password123", "Backend");
        memberService.deleteMemberByAdmin(member.getId());

        // 회원 목록 조회 (삭제된 회원도 포함되어야 함)
        ResultActions result = mvc
                .perform(get("/members"))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-18"))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.members[?(@.email == 'deleted@test.com')].isDeleted").value(true));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자의 회원 목록 조회 시도 - 실패")
    @WithMockUser(roles = "MENTEE")
    void t18() throws Exception {
        // 일반 사용자가 회원 목록 조회 시도
        ResultActions result = mvc
                .perform(get("/members"))
                .andDo(print());

        result.andExpect(status().isForbidden()); // 403 Forbidden 예상
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서 회원 목록 조회 시도 - 실패")
    void t19() throws Exception {
        // 인증 없이 회원 목록 조회 시도
        ResultActions result = mvc
                .perform(get("/members"))
                .andDo(print());

        result.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("관리자 회원 목록 조회 - 생성일 역순 정렬 확인")
    @WithMockUser(roles = "ADMIN")
    void t20() throws Exception {
        // 시간 차이를 두고 회원 생성
        Member member1 = memberService.joinMentee("first@test.com", "첫번째유저", "첫번째닉네임", "password123", "Backend");
        Thread.sleep(100); // 시간 차이를 위한 대기
        Member member2 = memberService.joinMentee("second@test.com", "두번째유저", "두번째닉네임", "password123", "Frontend");

        // 회원 목록 조회 (최신순 정렬)
        ResultActions result = mvc
                .perform(get("/members")
                        .param("size", "10"))
                .andDo(print());

        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.members").isArray());
        // 최신 회원이 먼저 나와야 함 (createDate DESC)
    }
}