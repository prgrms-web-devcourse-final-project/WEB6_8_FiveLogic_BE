package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.fixture.MemberTestFixture;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MentorSlotControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private MemberTestFixture memberFixture;
    @Autowired private MentoringFixture mentoringFixture;

    @Autowired private MentorSlotRepository mentorSlotRepository;
    @Autowired private AuthTokenService authTokenService;

    private static final String TOKEN = "accessToken";
    private static final String MENTOR_SLOT_URL = "/mentor-slot";

    private Mentor mentor;
    private String mentorToken;

    @BeforeEach
    void setUp() {
        // Mentor
        Member mentorMember = memberFixture.createMentorMember();
        mentor = memberFixture.createMentor(mentorMember);

        // // JWT 발급
        mentorToken = authTokenService.genAccessToken(mentorMember);

        // Mentoring
        mentoringFixture.createMentoring(mentor);

        // 2025-10-01 ~ 2025-10-05 10:00 ~ 11:30 (30분 단위 MentorSlot)
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 10, 1, 10, 0);
        mentoringFixture.createMentorSlots(mentor, baseDateTime, 1, 3);
    }

    @Test
    @DisplayName("멘토 슬롯 생성 성공")
    void createMentorSlotSuccess() throws Exception {
        String req = """
                    {
                        "mentorId": %d,
                        "startDateTime": "2025-09-30T15:00:00Z",
                        "endDateTime": "2025-09-30T16:00:00Z"
                    }
                    """.formatted(mentor.getId());

        ResultActions resultActions = mvc.perform(
            post(MENTOR_SLOT_URL)
                .cookie(new Cookie(TOKEN, mentorToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(req)
            )
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("createMentorSlot"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("멘토링 예약 일정을 등록했습니다."));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 멘토가 아닌 경우")
    void createMentorSlotFailNotMentor() throws Exception {
        Member menteeMember = memberFixture.createMenteeMember();
        String token  = authTokenService.genAccessToken(menteeMember);

        String req = """
                    {
                        "mentorId": %d,
                        "startDateTime": "2025-09-30T15:00:00Z",
                        "endDateTime": "2025-09-30T16:00:00Z"
                    }
                    """.formatted(mentor.getId());

        ResultActions resultActions = mvc.perform(
                post(MENTOR_SLOT_URL)
                    .cookie(new Cookie(TOKEN, token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("createMentorSlot"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 종료 일시가 시작 일시보다 빠른 경우")
    void createMentorSlotFailInValidDate() throws Exception {
        String req = """
                    {
                        "mentorId": %d,
                        "startDateTime": "2025-09-30T20:00:00Z",
                        "endDateTime": "2025-09-30T16:00:00Z"
                    }
                    """.formatted(mentor.getId());

        ResultActions resultActions = mvc.perform(
                post(MENTOR_SLOT_URL)
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("createMentorSlot"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-4"))
            .andExpect(jsonPath("$.msg").value("종료 일시는 시작 일시보다 이후여야 합니다."));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 기존 슬롯과 시간 겹치는 경우")
    void createMentorSlotFailOverlappingSlots() throws Exception {
        String req = """
                    {
                        "mentorId": %d,
                        "startDateTime": "2025-10-01T11:00:00",
                        "endDateTime": "2025-10-01T11:20:00"
                    }
                    """.formatted(mentor.getId());

        ResultActions resultActions = mvc.perform(
                post(MENTOR_SLOT_URL)
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("createMentorSlot"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.msg").value("선택한 시간은 이미 예약된 시간대입니다."));
    }
}