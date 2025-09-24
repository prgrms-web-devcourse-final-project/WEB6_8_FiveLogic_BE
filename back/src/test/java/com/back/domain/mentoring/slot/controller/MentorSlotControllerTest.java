package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.fixture.MemberTestFixture;
import com.back.fixture.MentoringFixture;
import com.back.global.exception.ServiceException;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private Mentoring mentoring;
    private List<MentorSlot> mentorSlots = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Mentor
        Member mentorMember = memberFixture.createMentorMember();
        mentor = memberFixture.createMentor(mentorMember);

        // // JWT 발급
        mentorToken = authTokenService.genAccessToken(mentorMember);

        // Mentoring
        mentoring = mentoringFixture.createMentoring(mentor);

        // 2025-10-01 ~ 2025-10-02 10:00 ~ 11:30 (30분 단위 MentorSlot)
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 10, 1, 10, 0);
        mentorSlots = mentoringFixture.createMentorSlots(mentor, baseDateTime, 2, 3);
    }

    // ===== 슬롯 생성 =====

    @Test
    @DisplayName("멘토 슬롯 생성 성공")
    void createMentorSlotSuccess() throws Exception {
        String startDateTime = "2025-09-30T15:00:00";
        String endDateTime = "2025-09-30T16:00:00";

        ResultActions resultActions = performCreateMentorSlot(mentor.getId(), mentorToken, startDateTime, endDateTime)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("멘토링 예약 일정을 등록했습니다."));

        MentorSlot mentorSlot = mentorSlotRepository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));

        resultActions
            .andExpect(jsonPath("$.data.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoringTitle").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.startDateTime").value(startDateTime))
            .andExpect(jsonPath("$.data.endDateTime").value(endDateTime))
            .andExpect(jsonPath("$.data.mentorSlotStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 멘토가 아닌 경우")
    void createMentorSlotFailNotMentor() throws Exception {
        Member menteeMember = memberFixture.createMenteeMember();
        String token  = authTokenService.genAccessToken(menteeMember);

        performCreateMentorSlot(mentor.getId(), token, "2025-09-30T15:00:00", "2025-09-30T16:00:00")
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 종료 일시가 시작 일시보다 빠른 경우")
    void createMentorSlotFailInValidDate() throws Exception {
        performCreateMentorSlot(mentor.getId(), mentorToken, "2025-09-30T20:00:00", "2025-09-30T16:00:00")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-4"))
            .andExpect(jsonPath("$.msg").value("종료 일시는 시작 일시보다 이후여야 합니다."));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 기존 슬롯과 시간 겹치는 경우")
    void createMentorSlotFailOverlappingSlots() throws Exception {
        performCreateMentorSlot(mentor.getId(), mentorToken, "2025-10-01T11:00:00", "2025-10-01T11:20:00")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.msg").value("선택한 시간은 이미 예약된 시간대입니다."));
    }


    // ===== 슬롯 수정 =====

    @Test
    @DisplayName("멘토 슬롯 수정 성공")
    void updateMentorSlotSuccess() throws Exception {
        MentorSlot mentorSlot = mentorSlots.getFirst();
        LocalDateTime updateEndDate = mentorSlot.getEndDateTime().minusMinutes(10);

        ResultActions resultActions = performUpdateMentorSlot(mentor.getId(), mentorToken, mentorSlot, updateEndDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String expectedEndDate = updateEndDate.format(formatter);

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토링 예약 일정이 수정되었습니다."))
            .andExpect(jsonPath("$.data.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoringTitle").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.endDateTime").value(expectedEndDate))
            .andExpect(jsonPath("$.data.mentorSlotStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("멘토 슬롯 수정 실패 - 기존 슬롯과 겹치는지 검사")
    void updateMentorSlotFail() throws Exception {
        MentorSlot mentorSlot = mentorSlots.getFirst();
        LocalDateTime updateEndDate = mentorSlots.get(1).getEndDateTime();

        performUpdateMentorSlot(mentor.getId(), mentorToken, mentorSlot, updateEndDate)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.msg").value("선택한 시간은 이미 예약된 시간대입니다."));
    }


    // ===== perform =====

    private ResultActions performCreateMentorSlot(Long mentorId, String token, String start, String end) throws Exception {
        String req = """
            {
                "mentorId": %d,
                "startDateTime": "%s",
                "endDateTime": "%s"
            }
            """.formatted(mentorId, start, end);

        return mvc.perform(
                post(MENTOR_SLOT_URL)
                    .cookie(new Cookie(TOKEN, token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("createMentorSlot"));
    }

    private ResultActions performUpdateMentorSlot(Long mentorId, String token, MentorSlot mentorSlot, LocalDateTime updateEndDate) throws Exception {
        String req = """
            {
                "mentorId": %d,
                "startDateTime": "%s",
                "endDateTime": "%s"
            }
            """.formatted(mentorId, mentorSlot.getStartDateTime(), updateEndDate);

        return mvc.perform(
                put(MENTOR_SLOT_URL + "/" + mentorSlot.getId())
                    .cookie(new Cookie(TOKEN, token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("updateMentorSlot"));
    }

}