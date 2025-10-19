package com.back.domain.roadmap.roadmap.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.roadmap.roadmap.service.MentorRoadmapService;
import com.back.fixture.MemberTestFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MentorRoadmapControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberTestFixture memberFixture;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private MentorRoadmapService mentorRoadmapService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "accessToken";
    private static final String MENTOR_ROADMAP_URL = "/mentor-roadmaps";

    private Member mentorMember;
    private Mentor mentor;
    private String mentorToken;

    @BeforeEach
    void setUp() {
        // 멘토 생성
        mentorMember = memberFixture.createMentorMember();
        mentor = memberFixture.createMentor(mentorMember);

        // JWT 발급
        mentorToken = authTokenService.genAccessToken(mentorMember);
    }

    // ===== 멘토 로드맵 생성 =====

    @Test
    @DisplayName("멘토 로드맵 생성 - 성공")
    void t1() throws Exception {

        String requestBody = """
            {
                "title": "백엔드 개발자 로드맵",
                "description": "Java 백엔드 개발자를 위한 학습 로드맵",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Java",
                        "learningAdvice": "객체지향 프로그래밍 언어 학습",
                        "recommendedResources": "Java의 정석, 이펙티브 자바",
                        "learningGoals": "Java 기본 문법 및 OOP 이해",
                        "difficulty": 3,
                        "importance": 5,
                        "hoursPerDay": 3,
                        "weeks": 4,
                        "stepOrder": 1
                    },
                    {
                        "taskId": null,
                        "taskName": "Spring Boot",
                        "learningAdvice": "Java 웹 애플리케이션 프레임워크",
                        "recommendedResources": "스프링 인 액션",
                        "learningGoals": "Spring Boot 기본 개념 이해",
                        "difficulty": 4,
                        "importance": 5,
                        "hoursPerDay": 4,
                        "weeks": 6,
                        "stepOrder": 2
                    }
                ]
            }
            """;

        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())  // ← HTTP 201 Created (ResponseAspect가 resultCode "201"을 파싱하여 설정)
                .andExpect(handler().handlerType(MentorRoadmapController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.msg").value("멘토 로드맵이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.mentorId").value(mentor.getId()))
                .andExpect(jsonPath("$.data.title").value("백엔드 개발자 로드맵"))
                .andExpect(jsonPath("$.data.description").value("Java 백엔드 개발자를 위한 학습 로드맵"))
                .andExpect(jsonPath("$.data.nodeCount").value(2))
                .andExpect(jsonPath("$.data.createDate").exists());
    }

    @Test
    @DisplayName("멘토 로드맵 생성 실패 - 중복 생성")
    void t2() throws Exception {
        String requestBody = createSampleRequestBody();
        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201"));

        // 동일 멘토가 다시 생성 시도
        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isConflict())  // ← HTTP 409 Conflict (ServiceException "409"를 GlobalExceptionHandler가 처리)
                .andExpect(jsonPath("$.resultCode").value("409"))
                .andExpect(jsonPath("$.msg").value("이미 로드맵이 존재합니다. 멘토는 하나의 로드맵만 생성할 수 있습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 생성 실패 - 빈 노드 리스트")
    void t3() throws Exception {
        String requestBody = """
            {
                "title": "제목",
                "description": "설명",
                "nodes": []
            }
            """;

        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("nodes-NotEmpty-로드맵 노드는 최소 1개 이상 필요합니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 생성 실패 - stepOrder 비연속")
    void t4() throws Exception {
        String requestBody = """
            {
                "title": "잘못된 로드맵",
                "description": "stepOrder가 비연속",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Java",
                        "learningAdvice": "1단계",
                        "stepOrder": 1
                    },
                    {
                        "taskId": null,
                        "taskName": "Spring",
                        "learningAdvice": "3단계",
                        "stepOrder": 3
                    }
                ]
            }
            """;

        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("stepOrder는 1부터 2 사이의 값이어야 합니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 생성 실패 - stepOrder 중복")
    void t5() throws Exception {
        String requestBody = """
            {
                "title": "잘못된 로드맵",
                "description": "stepOrder가 중복",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Java",
                        "learningAdvice": "1단계",
                        "stepOrder": 1
                    },
                    {
                        "taskId": null,
                        "taskName": "Spring",
                        "learningAdvice": "중복 1단계",
                        "stepOrder": 1
                    }
                ]
            }
            """;

        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("stepOrder에 중복된 값이 있습니다"));
    }

    @Test
    @DisplayName("멘토 로드맵 생성 실패 - 멘토 권한 없음 (멘티)")
    void t6() throws Exception {
        Member menteeMember = memberFixture.createMenteeMember();
        String menteeToken = authTokenService.genAccessToken(menteeMember);

        String requestBody = createSampleRequestBody();

        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, menteeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("멘토 로드맵 조회 (로드맵 ID) - 성공")
    void t7() throws Exception {
        Long roadmapId = createRoadmap();

        // When & Then
        mvc.perform(get(MENTOR_ROADMAP_URL + "/" + roadmapId)
                        .cookie(new Cookie(TOKEN, mentorToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MentorRoadmapController.class))
                .andExpect(handler().methodName("getById"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("멘토 로드맵 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(roadmapId))
                .andExpect(jsonPath("$.data.mentorId").value(mentor.getId()))
                .andExpect(jsonPath("$.data.title").value("백엔드 개발자 로드맵"))
                .andExpect(jsonPath("$.data.nodes").isArray())
                .andExpect(jsonPath("$.data.nodes.length()").value(2))
                .andExpect(jsonPath("$.data.nodes[0].taskName").value("Java"))
                .andExpect(jsonPath("$.data.nodes[0].stepOrder").value(1))
                .andExpect(jsonPath("$.data.nodes[1].taskName").value("Spring Boot"))
                .andExpect(jsonPath("$.data.nodes[1].stepOrder").value(2));
    }

    @Test
    @DisplayName("멘토 로드맵 조회 (멘토 ID) - 성공")
    void t8() throws Exception {
        createRoadmap();

        mvc.perform(get(MENTOR_ROADMAP_URL + "/mentor/" + mentor.getId())
                        .cookie(new Cookie(TOKEN, mentorToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MentorRoadmapController.class))
                .andExpect(handler().methodName("getByMentorId"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("멘토 로드맵 조회 성공"))
                .andExpect(jsonPath("$.data.mentorId").value(mentor.getId()))
                .andExpect(jsonPath("$.data.title").value("백엔드 개발자 로드맵"))
                .andExpect(jsonPath("$.data.nodes").isArray())
                .andExpect(jsonPath("$.data.nodes.length()").value(2));
    }

    @Test
    @DisplayName("멘토 로드맵 조회 실패 - 존재하지 않는 ID")
    void t9() throws Exception {
        Long nonExistentId = 99999L;

        mvc.perform(get(MENTOR_ROADMAP_URL + "/" + nonExistentId)
                        .cookie(new Cookie(TOKEN, mentorToken)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("로드맵을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 수정 - 성공")
    void t10() throws Exception {
        Long roadmapId = createRoadmap();

        String updateRequest = """
            {
                "title": "수정된 로드맵 제목",
                "description": "수정된 설명",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Python",
                        "learningAdvice": "프로그래밍 언어",
                        "stepOrder": 1
                    }
                ]
            }
            """;

        mvc.perform(put(MENTOR_ROADMAP_URL + "/" + roadmapId)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MentorRoadmapController.class))
                .andExpect(handler().methodName("update"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("멘토 로드맵이 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(roadmapId))
                .andExpect(jsonPath("$.data.title").value("수정된 로드맵 제목"))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                .andExpect(jsonPath("$.data.nodeCount").value(1));
    }

    @Test
    @DisplayName("멘토 로드맵 수정 - 성공 (기본 정보만 수정)")
    void t11() throws Exception {
        Long roadmapId = createRoadmap();

        String updateRequest = """
            {
                "title": "수정된 로드맵 제목",
                "description": "수정된 설명",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Java",
                        "learningAdvice": "객체지향 프로그래밍 언어 학습",
                        "stepOrder": 1
                    },
                    {
                        "taskId": null,
                        "taskName": "Spring Boot",
                        "learningAdvice": "Java 웹 애플리케이션 프레임워크",
                        "stepOrder": 2
                    }
                ]
            }
            """;

        mvc.perform(put(MENTOR_ROADMAP_URL + "/" + roadmapId)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 로드맵 제목"))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                .andExpect(jsonPath("$.data.nodeCount").value(2));
    }

    @Test
    @DisplayName("멘토 로드맵 수정 실패 - 권한 없음 (다른 멘토)")
    void t12() throws Exception {
        Long roadmapId = createRoadmap();

        Member otherMentorMember = memberFixture.createMentorMember();
        memberFixture.createMentor(otherMentorMember);
        String otherMentorToken = authTokenService.genAccessToken(otherMentorMember);

        String updateRequest = """
            {
                "title": "악의적 수정 시도",
                "description": "다른 멘토가 수정 시도",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Hacked",
                        "learningAdvice": "해킹 시도",
                        "stepOrder": 1
                    }
                ]
            }
            """;

        mvc.perform(put(MENTOR_ROADMAP_URL + "/" + roadmapId)
                        .cookie(new Cookie(TOKEN, otherMentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"))
                .andExpect(jsonPath("$.msg").value("본인의 로드맵만 수정할 수 있습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 수정 실패 - 존재하지 않는 ID")
    void t13() throws Exception {
        Long nonExistentId = 99999L;
        String updateRequest = createSampleRequestBody();

        mvc.perform(put(MENTOR_ROADMAP_URL + "/" + nonExistentId)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("로드맵을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 삭제 - 성공")
    void t14() throws Exception {
        Long roadmapId = createRoadmap();

        mvc.perform(delete(MENTOR_ROADMAP_URL + "/" + roadmapId)
                        .cookie(new Cookie(TOKEN, mentorToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MentorRoadmapController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("멘토 로드맵이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 삭제 실패 - 권한 없음 (다른 멘토)")
    void t15() throws Exception {
        Long roadmapId = createRoadmap();

        Member otherMentorMember = memberFixture.createMentorMember();
        memberFixture.createMentor(otherMentorMember);
        String otherMentorToken = authTokenService.genAccessToken(otherMentorMember);

        mvc.perform(delete(MENTOR_ROADMAP_URL + "/" + roadmapId)
                        .cookie(new Cookie(TOKEN, otherMentorToken)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"))
                .andExpect(jsonPath("$.msg").value("본인의 로드맵만 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 삭제 실패 - 존재하지 않는 ID")
    void t16() throws Exception {
        Long nonExistentId = 99999L;

        mvc.perform(delete(MENTOR_ROADMAP_URL + "/" + nonExistentId)
                        .cookie(new Cookie(TOKEN, mentorToken)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("로드맵을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("멘토 로드맵 목록 조회 - 페이징 기본 동작")
    void t17() throws Exception {
        createRoadmap();

        Member mentor2Member = memberFixture.createMentorMember();
        Mentor mentor2 = memberFixture.createMentor(mentor2Member);
        String mentor2Token = authTokenService.genAccessToken(mentor2Member);
        createRoadmapForMentor(mentor2Token);

        Member mentor3Member = memberFixture.createMentorMember();
        Mentor mentor3 = memberFixture.createMentor(mentor3Member);
        String mentor3Token = authTokenService.genAccessToken(mentor3Member);
        createRoadmapForMentor(mentor3Token);

        mvc.perform(get(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MentorRoadmapController.class))
                .andExpect(handler().methodName("getAllMentorRoadmaps"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("멘토 로드맵 목록 조회 성공"))
                .andExpect(jsonPath("$.data.mentorRoadmaps").isArray())
                .andExpect(jsonPath("$.data.mentorRoadmaps.length()").value(3))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("멘토 로드맵 목록 조회 - 키워드 검색")
    void t18() throws Exception {
        createRoadmap();

        Member mentor2Member = memberFixture.createMentorMember();
        Mentor mentor2 = memberFixture.createMentor(mentor2Member);
        String mentor2Token = authTokenService.genAccessToken(mentor2Member);
        createRoadmapWithTitle(mentor2Token, "프론트엔드 개발자 로드맵", "React 기반");

        mvc.perform(get(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .param("keyword", "백엔드")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mentorRoadmaps").isArray())
                .andExpect(jsonPath("$.data.mentorRoadmaps.length()").value(1))
                .andExpect(jsonPath("$.data.mentorRoadmaps[0].title").value("백엔드 개발자 로드맵"));
    }

    // ===== 헬퍼 메서드 =====

    // 샘플 로드맵 생성
    private Long createRoadmap() throws Exception {
        String requestBody = createSampleRequestBody();
        String response = mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, mentorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("data").get("id").asLong();
    }

    // 특정 멘토 토큰으로 로드맵 생성
    private void createRoadmapForMentor(String token) throws Exception {
        String requestBody = createSampleRequestBody();
        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    // 샘플 로드맵 생성
    private void createRoadmapWithTitle(String token, String title, String description) throws Exception {
        String requestBody = String.format("""
            {
                "title": "%s",
                "description": "%s",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Task 1",
                        "learningAdvice": "설명 1",
                        "stepOrder": 1
                    }
                ]
            }
            """, title, description);

        mvc.perform(post(MENTOR_ROADMAP_URL)
                        .cookie(new Cookie(TOKEN, token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    // 샘플 request 생성
    private String createSampleRequestBody() {
        return """
            {
                "title": "백엔드 개발자 로드맵",
                "description": "Java 백엔드 개발자를 위한 학습 로드맵",
                "nodes": [
                    {
                        "taskId": null,
                        "taskName": "Java",
                        "learningAdvice": "객체지향 프로그래밍 언어 학습",
                        "stepOrder": 1
                    },
                    {
                        "taskId": null,
                        "taskName": "Spring Boot",
                        "learningAdvice": "Java 웹 애플리케이션 프레임워크",
                        "stepOrder": 2
                    }
                ]
            }
            """;
    }
}