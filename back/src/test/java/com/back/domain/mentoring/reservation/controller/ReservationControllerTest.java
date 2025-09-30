package com.back.domain.mentoring.reservation.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.error.ReservationErrorCode;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
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

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private MemberTestFixture memberFixture;
    @Autowired private MentoringTestFixture mentoringFixture;

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private AuthTokenService authTokenService;

    private static final String TOKEN = "accessToken";
    private static final String RESERVATION_URL = "/reservations";

    private Mentor mentor;
    private Mentee mentee;
    private Mentoring mentoring;
    private MentorSlot mentorSlot;
    private String menteeToken;

    @BeforeEach
    void setUp() {
        // Mentor
        Member mentorMember = memberFixture.createMentorMember();
        mentor = memberFixture.createMentor(mentorMember);

        // Mentee
        Member menteeMember = memberFixture.createMenteeMember();
        mentee = memberFixture.createMentee(menteeMember);
        menteeToken = authTokenService.genAccessToken(menteeMember);

        // Mentoring, MentorSlot
        mentoring = mentoringFixture.createMentoring(mentor);
        mentorSlot = mentoringFixture.createMentorSlot(mentor);
    }

    @Test
    @DisplayName("멘티가 멘토에게 예약 신청 성공")
    void createReservationSuccess() throws Exception {
        ResultActions resultActions = performCreateReservation();

        Reservation reservation = reservationRepository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new ServiceException(ReservationErrorCode.NOT_FOUND_RESERVATION));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String expectedStart = mentorSlot.getStartDateTime().truncatedTo(ChronoUnit.SECONDS).format(formatter);
        String expectedEnd = mentorSlot.getEndDateTime().truncatedTo(ChronoUnit.SECONDS).format(formatter);

        resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("예약 신청이 완료되었습니다."))
            .andExpect(jsonPath("$.data.reservation.reservationId").value(reservation.getId()))
            .andExpect(jsonPath("$.data.reservation.status").value("PENDING"))
            .andExpect(jsonPath("$.data.reservation.preQuestion").value(reservation.getPreQuestion()))
            .andExpect(jsonPath("$.data.reservation.mentorSlotId").value(mentorSlot.getId()))
            .andExpect(jsonPath("$.data.reservation.startDateTime").value(expectedStart))
            .andExpect(jsonPath("$.data.reservation.endDateTime").value(expectedEnd));
    }


    // ===== perform =====

    private ResultActions performCreateReservation() throws Exception {
        String req = """
            {
                "mentorId": %d,
                "mentorSlotId": %d,
                "mentoringId": %d,
                "preQuestion": "질문"
            }
            """.formatted(mentor.getId(), mentorSlot.getId(), mentoring.getId());

        return mvc.perform(
                post(RESERVATION_URL)
                    .cookie(new Cookie(TOKEN, menteeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print())
            .andExpect(handler().handlerType(ReservationController.class))
            .andExpect(handler().methodName("createReservation"));
    }

}