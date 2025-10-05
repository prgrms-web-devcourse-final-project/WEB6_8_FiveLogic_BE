package com.back.domain.mentoring.slot.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.slot.dto.response.MentorSlotDto;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
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
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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

        // 2일간 10:00 ~ 11:30 (30분 단위 MentorSlot)
        LocalDateTime baseDateTime = LocalDateTime.of(
            LocalDate.now().plusMonths(2),
            LocalTime.of(10, 0, 0)
        ).truncatedTo(ChronoUnit.SECONDS);
        mentorSlots = mentoringFixture.createMentorSlots(mentor, baseDateTime, 2, 3, 30L);
    }

    // ===== 슬롯 목록 조회 =====
    @Test
    @DisplayName("멘토가 본인의 모든 슬롯 목록 조회 성공")
    void getMyMentorSlotsSuccess() throws Exception {
        LocalDateTime startDate = mentorSlots.getFirst().getStartDateTime();
        LocalDateTime endDate = mentorSlots.getLast().getEndDateTime();

        ResultActions resultActions = mvc.perform(
                get(MENTOR_SLOT_URL)
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .param("startDate", startDate.toLocalDate().format(DateTimeFormatter.ISO_DATE))
                    .param("endDate", endDate.toLocalDate().plusDays(1).format(DateTimeFormatter.ISO_DATE))
            )
            .andDo(print());

        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("getMyMentorSlots"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("나의 모든 일정 목록을 조회하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(mentorSlots.size()));
    }

    @Test
    @DisplayName("멘토의 예약 가능한 슬롯 목록 조회(멘티) 성공")
    void getAvailableMentorSlotsSuccess() throws Exception {
        // 캘린더 기준 (월)
        LocalDateTime startDate = mentorSlots.getFirst().getStartDateTime();;
        LocalDateTime endDate = mentorSlots.getLast().getEndDateTime();

        Member menteeMember = memberFixture.createMenteeMember();
        String token  = authTokenService.genAccessToken(menteeMember);

        ResultActions resultActions = mvc.perform(
                get(MENTOR_SLOT_URL + "/available/" + mentor.getId())
                    .cookie(new Cookie(TOKEN, token))
                    .param("startDate", startDate.toLocalDate().format(DateTimeFormatter.ISO_DATE))
                    .param("endDate", endDate.toLocalDate().plusDays(1).format(DateTimeFormatter.ISO_DATE))
            )
            .andDo(print());

        resultActions
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(MentorSlotController.class))
            .andExpect(handler().methodName("getAvailableMentorSlots"))
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정 목록을 조회하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(mentorSlots.size()));
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
            .andExpect(jsonPath("$.data.mentorings[0].mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentorings[0].title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentorSlot.startDateTime").value(mentorSlot.getStartDateTime().format(formatter)))
            .andExpect(jsonPath("$.data.mentorSlot.endDateTime").value(mentorSlot.getEndDateTime().format(formatter)))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotStatus").value(mentorSlot.getStatus().name()));
    }

    // ===== 슬롯 생성 =====

    @Test
    @DisplayName("멘토 슬롯 생성 성공")
    void createMentorSlotSuccess() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(
            LocalDate.now().plusDays(2),
            LocalTime.of(10, 0, 0)
        ).truncatedTo(ChronoUnit.SECONDS);

        String startDateTime = baseDateTime.format(formatter);
        String endDateTime = baseDateTime.plusHours(1).format(formatter);

        ResultActions resultActions = performCreateMentorSlot(mentor.getId(), mentorToken, startDateTime, endDateTime)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정을 등록했습니다."));

        MentorSlot mentorSlot = mentorSlotRepository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new ServiceException(MentorSlotErrorCode.NOT_FOUND_MENTOR_SLOT));

        resultActions
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentor.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentorings[0].mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentorings[0].title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentorSlot.startDateTime").value(startDateTime))
            .andExpect(jsonPath("$.data.mentorSlot.endDateTime").value(endDateTime))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotStatus").value("AVAILABLE"));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 멘토가 아닌 경우")
    void createMentorSlotFailNotMentor() throws Exception {
        Member menteeMember = memberFixture.createMenteeMember();
        String token  = authTokenService.genAccessToken(menteeMember);

        LocalDateTime baseDateTime = LocalDateTime.of(
            LocalDate.now().plusDays(2),
            LocalTime.of(10, 0, 0)
        ).truncatedTo(ChronoUnit.SECONDS);

        String startDateTime = baseDateTime.format(formatter);
        String endDateTime = baseDateTime.plusHours(1).format(formatter);

        performCreateMentorSlot(mentor.getId(), token, startDateTime, endDateTime)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("멘토 슬롯 생성 실패 - 종료 일시가 시작 일시보다 빠른 경우")
    void createMentorSlotFailInValidDate() throws Exception {
        LocalDateTime baseDateTime = LocalDateTime.of(
            LocalDate.now().plusDays(2),
            LocalTime.of(10, 0, 0)
        ).truncatedTo(ChronoUnit.SECONDS);

        String startDateTime = baseDateTime.format(formatter);
        String endDateTime = baseDateTime.minusHours(1).format(formatter);

        performCreateMentorSlot(mentor.getId(), mentorToken, startDateTime, endDateTime)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("400-4"))
            .andExpect(jsonPath("$.msg").value("종료 일시는 시작 일시보다 이후여야 합니다."));
    }


    // ===== 슬롯 반복 생성 =====
    @Test
    @DisplayName("멘토 슬롯 반복 생성 성공")
    void createMentorSlotRepetitionSuccess() throws Exception {
        LocalDate startDate = LocalDate.now().plusWeeks(1);
        LocalDate endDate = startDate.plusMonths(1);


        String req = """
        {
            "repeatStartDate": "%s",
            "repeatEndDate": "%s",
            "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
            "startTime": "10:00:00",
            "endTime": "11:00:00"
        }
        """.formatted(
            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );

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

        long afterCount = mentorSlotRepository.countByMentorId(mentor.getId());

        // 월/수/금
        long expectedCount = countDaysOfWeek(startDate, endDate,
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        assertThat(afterCount - beforeCount).isEqualTo(expectedCount);

        List<MentorSlotDto> createdSlots = mentorSlotRepository.findMySlots(
            mentor.getId(),
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()
        );
        assertThat(createdSlots).hasSize((int) expectedCount);

        // 모든 슬롯이 월/수/금인지 검증
        Set<DayOfWeek> actualDaysOfWeek = createdSlots.stream()
            .map(slot -> slot.startDateTime().getDayOfWeek())
            .collect(Collectors.toSet());

        assertThat(actualDaysOfWeek).containsExactlyInAnyOrder(
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY
        );

        // 시간 검증
        MentorSlotDto firstSlot = createdSlots.getFirst();
        assertThat(firstSlot.startDateTime().getHour()).isEqualTo(10);
        assertThat(firstSlot.endDateTime().getHour()).isEqualTo(11);
    }


    // ===== 슬롯 수정 =====

    @Test
    @DisplayName("멘토 슬롯 수정 성공 - 예약이 없는 경우")
    void updateMentorSlotSuccess() throws Exception {
        MentorSlot mentorSlot = mentorSlots.getFirst();
        LocalDateTime updateEndDate = mentorSlot.getEndDateTime().minusMinutes(10);

        ResultActions resultActions = performUpdateMentorSlot(mentor.getId(), mentorToken, mentorSlot, updateEndDate);

        String expectedEndDate = updateEndDate.format(formatter);

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토의 예약 가능 일정이 수정되었습니다."))
            .andExpect(jsonPath("$.data.mentorSlot.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.mentor.mentorId").value(mentorSlot.getMentor().getId()))
            .andExpect(jsonPath("$.data.mentorings[0].mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentorings[0].title").value(mentoring.getTitle()))
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

    private long countDaysOfWeek(LocalDate start, LocalDate end, Set<DayOfWeek> daysOfWeek) {
        return start.datesUntil(end.plusDays(1))
            .filter(date -> daysOfWeek.contains(date.getDayOfWeek()))
            .count();
    }
}