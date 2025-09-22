package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.service.MentoringService;
import com.back.fixture.MemberFixture;
import com.back.fixture.MentoringFixture;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MentoringControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private MemberFixture memberFixture;
    @Autowired private MentoringFixture mentoringFixture;
    @Autowired private MentoringService mentoringService;

    private static final String TOKEN = "accessToken";
    private static final String MENTORING_URL = "/mentoring";

    private Mentor mentor;
    private String mentorToken;
    private String menteeToken;

    @BeforeEach
    void setUp() {
        // Mentor
        Member mentorMember = memberFixture.createMentor();
        mentor = memberFixture.createMentorProfile(mentorMember);

        // Mentee
        Member menteeMember = memberFixture.createMentee();

        // JWT 발급
        mentorToken = memberFixture.getAccessToken(mentorMember);
        menteeToken = memberFixture.getAccessToken(menteeMember);
    }

    @Test
    @DisplayName("멘토링 생성 성공")
    void createMentoringSuccess() throws Exception {
        ResultActions resultActions = performCreateMentoring(mentorToken);

        Mentoring mentoring = mentoringService.getLastestMentoring();
        Mentor mentor = mentoring.getMentor();

        resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("createMentoring"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("멘토링이 생성 완료되었습니다."))

            // Mentoring 정보 검증
            .andExpect(jsonPath("$.data.mentoringDetailDto.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoringDetailDto.title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentoringDetailDto.tags").value(mentoring.getTags()))
            .andExpect(jsonPath("$.data.mentoringDetailDto.bio").value(mentoring.getBio()))
            .andExpect(jsonPath("$.data.mentoringDetailDto.thumb").value(mentoring.getThumb()))

            // Mentor 정보 검증
            .andExpect(jsonPath("$.data.mentorDto.mentorId").value(mentor.getId()))
            .andExpect(jsonPath("$.data.mentorDto.name").value(mentor.getMember().getName()))
            .andExpect(jsonPath("$.data.mentorDto.rate").value(mentor.getRate()))
            .andExpect(jsonPath("$.data.mentorDto.careerYears").value(mentor.getCareerYears()));
    }

    @Test
    @DisplayName("멘토링 생성 실패 - 멘토가 아닌 경우")
    void createMentoringFailNotMentorRole() throws Exception {
        performCreateMentoring(menteeToken)
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("createMentoring"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("멘토링 생성 실패 - Mentor 권한이지만 Mentor 엔티티가 없는 경우")
    void createMentoringFailNotMentor() throws Exception {
        Member mentorMember = memberFixture.createMentor();
        String token = memberFixture.getAccessToken(mentorMember);

        performCreateMentoring(token)
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("createMentoring"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-1"))
            .andExpect(jsonPath("$.msg").value("멘토를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("멘토링 생성 실패 - 멘토당 멘토링 1개 제한")
    void createMentoringFailDuplicate() throws Exception {
        mentoringFixture.createMentoring(mentor);

        performCreateMentoring(mentorToken)
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("createMentoring"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.msg").value("이미 멘토링 정보가 존재합니다."));
    }


    // ===== perform =====

    private ResultActions performCreateMentoring(String token) throws Exception {
        String req = """
                    {
                        "title": "Spring Boot 멘토링",
                        "tags": ["Spring", "Java"],
                        "bio": "Spring Boot를 활용한 백엔드 개발 입문",
                        "thumb": "https://example.com/thumb.jpg"
                    }
                    """;

        return mvc
            .perform(
                post(MENTORING_URL)
                    .cookie(new Cookie(TOKEN, token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print());
    }
}