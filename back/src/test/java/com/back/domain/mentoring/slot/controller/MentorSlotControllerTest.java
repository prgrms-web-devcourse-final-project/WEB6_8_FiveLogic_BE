package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.error.MentorSlotErrorCode;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.fixture.MemberTestFixture;
import com.back.fixture.mentoring.MentoringTestFixture;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MentorSlotControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private MemberTestFixture memberFixture;
    @Autowired private MentoringTestFixture mentoringFixture;

    @Autowired private MentorSlotRepository mentorSlotRepository;
    @Autowired private AuthTokenService authTokenService;

    private static final String TOKEN = "accessToken";
    private static final String MENTOR_SLOT_URL = "/mentor-slots";

    private Mentor mentor;
    private String mentorToken;
    private Mentoring mentoring;
    private List<MentorSlot> mentorSlots = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Mentor
        Member mentorMember = memberFixture.createMentorMember();
        mentor = memberFixture.createMentor(mentorMember);

        // JWT 발급
        mentorToken = authTokenService.genAccessToken(mentorMember);

        // Mentoring
        mentoring = mentoringFixture.createMentoring(mentor);

        // 2025-10-01 ~ 2025-10-02 10:00 ~ 11:30 (30분 단위 MentorSlot)
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 10, 1, 10, 0);
        mentorSlots = mentoringFixture.createMentorSlots(mentor, baseDateTime, 2, 3);
    }

    // ===== 슬롯 목록 조회 =====
    @Test
    @DisplayName("멘토가 본인의 모든 슬롯 목록 조회 성공")
    void getMyMentorSlotsSuccess() throws Exception {
        // 캘린더 기준 (월)
        LocalDateTime startDate = LocalDateTime.of(2025, 8, 31, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 5, 0, 0);

        // 경계값
        mentoringFixture.createMentorSlot(mentor, endDate.minusMinutes(1), endDate.plusMinutes(10));
        mentoringFixture.createMentorSlot(mentor, startDate.minusMinutes(10), startDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        ResultActions resultActions = mvc.perform(
                get(MENTOR_SLOT_URL)
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .param("startDate", startDate.format(formatter))
                    .param("endDate", endDate.format(formatter))
            )
            .andDo(print());

        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("getMyMentorSlots"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("나의 모든 일정 목록을 조회하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(8));
    }

    @Test
    @DisplayName("멘토의 예약 가능한 슬롯 목록 조회(멘티) 성공")
    void getAvailableMentorSlotsSuccess() throws Exception {
        // 캘린더 기준 (월)
        LocalDateTime startDate = LocalDateTime.of(2025, 8, 31, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 5, 0, 0);

        // 경계값
        mentoringFixture.createMentorSlot(mentor, endDate.minusMinutes(1), endDate.plusMinutes(10));
        mentoringFixture.createMentorSlot(mentor, startDate.minusMinutes(10), startDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Member menteeMember = memberFixture.createMenteeMember();
        String token  = authTokenService.genAccessToken(menteeMember);

        ResultActions resultActions = mvc.perform(
                get(MENTOR_SLOT_URL + "/available/" + mentor.getId())
                    .cookie(new Cookie(TOKEN, token))
                    .param("startDate", startDate.format(formatter))
                    .param("endDate", endDate.format(formatter))
            )
            .andDo(print());

        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("getAvailableMentorSlots"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정 목록을 조회하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(8));
    }


    // ===== 슬롯 조회 =====
    @Test
    @DisplayName("멘토 슬롯 조회 성공")
    void getMentorSlotSuccess() throws Exception {
        MentorSlot mentorSlot = mentorSlots.getFirst();

        ResultActions resultActions = mvc.perform(
            get(MENTOR_SLOT_URL + "/" + mentorSlot.getId())
                .cookie(new Cookie(TOKEN, mentorToken))
            )
            .andDo(print());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("getMentorSlot"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정을 조회하였습니다."))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentor.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentoring.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoring.title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentorSlot.startDateTime").value(mentorSlot.getStartDateTime().format(formatter)))
            .andExpect(jsonPath("$.data.mentorSlot.endDateTime").value(mentorSlot.getEndDateTime().format(formatter)))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotStatus").value(mentorSlot.getStatus().name()));
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
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정을 등록했습니다."));

        MentorSlot mentorSlot = mentorSlotRepository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));

        resultActions
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentor.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentoring.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoring.title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentorSlot.startDateTime").value(startDateTime))
            .andExpect(jsonPath("$.data.mentorSlot.endDateTime").value(endDateTime))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotStatus").value("AVAILABLE"));
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


    // ===== 슬롯 반복 생성 =====
    @Test
    @DisplayName("멘토 슬롯 반복 생성 성공")
    void createMentorSlotRepetitionSuccess() throws Exception {
        String req = """
        {
            "repeatStartDate": "2025-11-01",
            "repeatEndDate": "2025-11-30",
            "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
            "startTime": "10:00:00",
            "endTime": "11:00:00"
        }
        """;

        long beforeCount = mentorSlotRepository.countByMentorId(mentor.getId());

        mvc.perform(
                post(MENTOR_SLOT_URL + "/repetition")
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("반복 일정을 등록했습니다."));

        // 11월 월/수/금 = 13개
        long afterCount = mentorSlotRepository.countByMentorId(mentor.getId());
        assertThat(afterCount - beforeCount).isEqualTo(12);

        List<MentorSlot> createdSlots = mentorSlotRepository.findMySlots(
            mentor.getId(),
            LocalDateTime.of(2025, 11, 1, 0, 0),
            LocalDateTime.of(2025, 12, 1, 0, 0)
        );
        assertThat(createdSlots).hasSize(12);

        // 모든 슬롯이 월/수/금인지 검증
        Set<DayOfWeek> actualDaysOfWeek = createdSlots.stream()
            .map(slot -> slot.getStartDateTime().getDayOfWeek())
            .collect(Collectors.toSet());

        assertThat(actualDaysOfWeek).containsExactlyInAnyOrder(
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY
        );

        // 시간 검증
        MentorSlot firstSlot = createdSlots.getFirst();
        assertThat(firstSlot.getStartDateTime().getHour()).isEqualTo(10);
        assertThat(firstSlot.getEndDateTime().getHour()).isEqualTo(11);
    }


    // ===== 슬롯 수정 =====

    @Test
    @DisplayName("멘토 슬롯 수정 성공 - 예약이 없는 경우")
    void updateMentorSlotSuccess() throws Exception {
        MentorSlot mentorSlot = mentorSlots.getFirst();
        LocalDateTime updateEndDate = mentorSlot.getEndDateTime().minusMinutes(10);

        ResultActions resultActions = performUpdateMentorSlot(mentor.getId(), mentorToken, mentorSlot, updateEndDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String expectedEndDate = updateEndDate.format(formatter);

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정이 수정되었습니다."))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentor.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentoring.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoring.title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentorSlot.endDateTime").value(expectedEndDate))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotStatus").value("AVAILABLE"));
    }


    // ===== delete =====

    @Test
    @DisplayName("멘토 슬롯 삭제 성공")
    void deleteMentorSlotSuccess() throws Exception {
        long beforeCnt = mentorSlotRepository.countByMentorId(mentor.getId());
        MentorSlot mentorSlot = mentorSlots.getFirst();

        ResultActions resultActions = performDeleteMentorSlot(mentorSlot, mentorToken);

        long afterCnt = mentorSlotRepository.countByMentorId(mentor.getId());

        resultActions
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("deleteMentorSlot"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정이 삭제되었습니다."));

        assertThat(afterCnt).isEqualTo(beforeCnt - 1);
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

    private ResultActions performDeleteMentorSlot(MentorSlot mentorSlot, String token) throws Exception {
        return mvc.perform(
                delete(MENTOR_SLOT_URL + "/" + mentorSlot.getId())
                    .cookie(new Cookie(TOKEN, token))
            )
            .andDo(print())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("deleteMentorSlot"));
    }
}