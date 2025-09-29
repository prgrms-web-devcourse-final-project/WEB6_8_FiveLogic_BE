package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.request.MentoringRequest;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.error.MentoringErrorCode;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import com.back.fixture.MemberTestFixture;
import com.back.fixture.mentoring.MentoringTestFixture;
import com.back.global.exception.ServiceException;
import com.back.standard.util.Ut;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MentoringControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private MemberTestFixture memberFixture;
    @Autowired private MentoringTestFixture mentoringFixture;

    @Autowired private MentoringRepository mentoringRepository;
    @Autowired private MentorSlotRepository mentorSlotRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private AuthTokenService authTokenService;

    private static final String TOKEN = "accessToken";
    private static final String MENTORING_URL = "/mentorings";

    private Mentor mentor;
    private String mentorToken;
    private String menteeToken;

    @BeforeEach
    void setUp() {
        // Mentor
        Member mentorMember = memberFixture.createMentorMember();
        mentor = memberFixture.createMentor(mentorMember);

        // Mentee
        Member menteeMember = memberFixture.createMenteeMember();
        memberFixture.createMentee(menteeMember);

        // JWT 발급
        mentorToken = authTokenService.genAccessToken(mentorMember);
        menteeToken = authTokenService.genAccessToken(menteeMember);
    }

    // ===== 멘토링 다건 조회 ====
    @Test
    @DisplayName("멘토링 다건 조회 - 기본 페이징")
    void getMentoringsSuccess() throws Exception {
        mentoringFixture.createMentorings(mentor, 15);

        // TODO: 일반 조회, 목록 조회는 쿠키 없어도 가능하게 설정 필요
        performGetMentorings(null, "0")
            .andExpect(jsonPath("$.data.mentorings").isArray())
            .andExpect(jsonPath("$.data.mentorings.length()").value(10))
            .andExpect(jsonPath("$.data.currentPage").value(0))
            .andExpect(jsonPath("$.data.totalPage").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(15))
            .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    @DisplayName("멘토링 다건 조회 - 두번 째 페이지")
    void getMentoringsSuccessSecondPage() throws Exception {
        mentoringFixture.createMentorings(mentor, 15);

        performGetMentorings(null, "1")
            .andExpect(jsonPath("$.data.mentorings").isArray())
            .andExpect(jsonPath("$.data.mentorings.length()").value(5))
            .andExpect(jsonPath("$.data.currentPage").value(1))
            .andExpect(jsonPath("$.data.totalPage").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(15))
            .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("멘토링 다건 조회 - 빈 결과")
    void getMentoringsSuccessEmpty() throws Exception {
        performGetMentorings(null, "0")
            .andExpect(jsonPath("$.data.mentorings").isArray())
            .andExpect(jsonPath("$.data.mentorings.length()").value(0))
            .andExpect(jsonPath("$.data.totalPage").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("멘토링 다건 조회 - 멘토명 검색")
    void getMentoringsSuccessSearchMentor() throws Exception {
        Member mentorMember = memberFixture.createMentorMember();
        Mentor mentor2 = memberFixture.createMentor(mentorMember);

        mentoringFixture.createMentorings(mentor, 8);
        mentoringFixture.createMentorings(mentor2, 3);

        performGetMentorings(mentorMember.getName(), "0")
            .andExpect(jsonPath("$.data.mentorings").isArray())
            .andExpect(jsonPath("$.data.mentorings.length()").value(3))
            .andExpect(jsonPath("$.data.currentPage").value(0))
            .andExpect(jsonPath("$.data.totalPage").value(1))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("멘토링 다건 조회 - 멘토링 검색")
    void getMentoringsSuccessSearchMentoring() throws Exception {
        performGetMentorings("테스트 멘토링 1", "0")
            .andExpect(jsonPath("$.data.mentorings").isArray());
    }

    @Test
    @DisplayName("멘토링 다건 조회 - 검색 결과 없는 경우")
    void getMentoringsSuccessSearchEmpty() throws Exception {
        performGetMentorings("mentee", "0")
            .andExpect(jsonPath("$.data.mentorings").isArray())
            .andExpect(jsonPath("$.data.mentorings.length()").value(0))
            .andExpect(jsonPath("$.data.totalPage").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }


    // ===== 멘토링 단건 조회 =====
    @Test
    @DisplayName("멘토링 조회 성공")
    void getMentoringSuccess() throws Exception {
        Mentoring mentoring = mentoringFixture.createMentoring(mentor);

        ResultActions resultActions = mvc
            .perform(
                get(MENTORING_URL + "/" + mentoring.getId())
                    .cookie(new Cookie(TOKEN, mentorToken))
            ).andDo(print());

        resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("getMentoring"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토링을 조회하였습니다."));
    }
    
    
    // ===== 멘토링 생성 ======

    @Test
    @DisplayName("멘토링 생성 성공")
    void createMentoringSuccess() throws Exception {
        ResultActions resultActions = performCreateMentoring(mentorToken);

        Mentoring mentoring = mentoringRepository.findTopByOrderByIdDesc()
            .orElseThrow(() -> new ServiceException(MentoringErrorCode.NOT_FOUND_MENTORING));
        Mentor mentorOfMentoring = mentoring.getMentor();

        resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("createMentoring"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resultCode").value("201"))
            .andExpect(jsonPath("$.msg").value("멘토링이 생성 완료되었습니다."))

            // Mentoring 정보 검증
            .andExpect(jsonPath("$.data.mentoring.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoring.title").value(mentoring.getTitle()))
            .andExpect(jsonPath("$.data.mentoring.tags").value(mentoring.getTags()))
            .andExpect(jsonPath("$.data.mentoring.bio").value(mentoring.getBio()))
            .andExpect(jsonPath("$.data.mentoring.thumb").value(mentoring.getThumb()))

            // Mentor 정보 검증
            .andExpect(jsonPath("$.data.mentor.mentorId").value(mentorOfMentoring.getId()))
            .andExpect(jsonPath("$.data.mentor.name").value(mentorOfMentoring.getMember().getName()))
            .andExpect(jsonPath("$.data.mentor.rate").value(mentorOfMentoring.getRate()))
            .andExpect(jsonPath("$.data.mentor.careerYears").value(mentorOfMentoring.getCareerYears()));
    }

    @Test
    @DisplayName("멘토링 생성 실패 - 멘토가 아닌 경우")
    void createMentoringFailNotMentorRole() throws Exception {
        performCreateMentoring(menteeToken)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.resultCode").value("403-1"))
            .andExpect(jsonPath("$.msg").value("접근 권한이 없습니다."));
    }

    @Test
    @DisplayName("멘토링 생성 실패 - Mentor 권한이지만 Mentor 엔티티가 없는 경우")
    void createMentoringFailNotMentor() throws Exception {
        Member mentorMember = memberFixture.createMentorMember();
        String token = authTokenService.genAccessToken(mentorMember);

        performCreateMentoring(token)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-2"))
            .andExpect(jsonPath("$.msg").value("멘토를 찾을 수 없습니다."));
    }


    // ===== 멘토링 수정 =====

    @Test
    @DisplayName("멘토링 수정 성공")
    void updateMentoringSuccess() throws Exception {
        Mentoring mentoring = mentoringFixture.createMentoring(mentor);

        MentoringRequest reqDto = new MentoringRequest(
            "Next.js 멘토링",
            List.of("Next.js", "React"),
            "Next.js를 활용한 프론트 개발 입문",
            "https://example.com/thumb.jpg"
        );

        ResultActions resultActions = mvc.perform(
                put(MENTORING_URL + "/" + mentoring.getId())
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(Ut.json.toString(reqDto))
            )
            .andDo(print());

        resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("updateMentoring"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토링이 수정되었습니다."))

            // Mentoring 정보 검증
            .andExpect(jsonPath("$.data.mentoring.mentoringId").value(mentoring.getId()))
            .andExpect(jsonPath("$.data.mentoring.title").value(reqDto.title()))
            .andExpect(jsonPath("$.data.mentoring.tags[0]").value(reqDto.tags().get(0)))
            .andExpect(jsonPath("$.data.mentoring.tags[1]").value(reqDto.tags().get(1)))
            .andExpect(jsonPath("$.data.mentoring.bio").value(reqDto.bio()))
            .andExpect(jsonPath("$.data.mentoring.thumb").value(reqDto.thumb()));
    }

    @Test
    @DisplayName("멘토링 수정 실패 - 멘토링 존재하지 않는 경우")
    void updateMentoringFailNotMentoring() throws Exception {
        long nonId = Long.MAX_VALUE;

        performUpdateMentoring(nonId, mentorToken)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-2"))
            .andExpect(jsonPath("$.msg").value("멘토링을 찾을 수 없습니다."));
    }


    // ===== 멘토링 삭제 =====

    @Test
    @DisplayName("멘토링 삭제 성공 - 연관 엔티티 없는 경우")
    void deleteMentoringSuccess() throws Exception {
        Mentoring mentoring = mentoringFixture.createMentoring(mentor);

        long preCnt = mentoringRepository.count();

        ResultActions resultActions = mvc.perform(
                delete(MENTORING_URL + "/" + mentoring.getId())
                    .cookie(new Cookie(TOKEN, mentorToken))
            )
            .andDo(print());

        long afterCnt = mentoringRepository.count();

        resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("deleteMentoring"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토링이 삭제되었습니다."));

        assertThat(preCnt - afterCnt).isEqualTo(1);
        assertThat(mentoringRepository.findById(mentoring.getId())).isEmpty();
        assertThat(reservationRepository.existsByMentoringId(mentoring.getId())).isFalse();
        assertThat(mentorSlotRepository.existsByMentorId(mentor.getId())).isFalse();
    }

    @Test
    @DisplayName("멘토링 삭제 성공 - 멘토 슬롯이 있는 경우")
    void deleteMentoringSuccessExistsMentorSlot() throws Exception {
        Mentoring mentoring = mentoringFixture.createMentoring(mentor);
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 10, 1, 10, 0);
        mentoringFixture.createMentorSlots(mentor, baseDateTime, 3, 2);

        long preMentoringCnt = mentoringRepository.count();
        long preSlotCnt = mentorSlotRepository.count();

        ResultActions resultActions = mvc.perform(
                delete(MENTORING_URL + "/" + mentoring.getId())
                    .cookie(new Cookie(TOKEN, mentorToken))
            )
            .andDo(print());

        long afterMentoringCnt = mentoringRepository.count();
        long afterSlotCnt = mentorSlotRepository.count();

        resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("deleteMentoring"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토링이 삭제되었습니다."));

        assertThat(preMentoringCnt - afterMentoringCnt).isEqualTo(1);
        assertThat(preSlotCnt - afterSlotCnt).isEqualTo(6);
        assertThat(mentoringRepository.findById(mentoring.getId())).isEmpty();
        assertThat(reservationRepository.existsByMentoringId(mentoring.getId())).isFalse();
        assertThat(mentorSlotRepository.existsByMentorId(mentor.getId())).isFalse();
    }

    @Test
    @DisplayName("멘토링 삭제 실패 - 멘토링 존재하지 않는 경우")
    void deleteMentoringFailNotMentoring() throws Exception {
        long nonId = Long.MAX_VALUE;

        performDeleteMentoring(nonId, mentorToken)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("404-2"))
            .andExpect(jsonPath("$.msg").value("멘토링을 찾을 수 없습니다."));
    }


    // ===== perform =====

    private ResultActions performGetMentorings(String keyword, String page) throws Exception {
        ResultActions resultActions = mvc
            .perform(
                get(MENTORING_URL)
                    .cookie(new Cookie(TOKEN, mentorToken))
                    .param("page", page)
                    .param("size", "10")
                    .param("keyword", keyword)
            ).andDo(print());

        return resultActions
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("getMentorings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200"))
            .andExpect(jsonPath("$.msg").value("멘토링 목록을 조회하였습니다."));
    }

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
            .andDo(print())
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("createMentoring"));
    }

    private ResultActions performUpdateMentoring(Long mentoringId, String token) throws Exception {
        String req = """
                    {
                        "title": "Next.js 멘토링",
                        "tags": ["Next.js", "React"],
                        "bio": "Next.js를 활용한 프론트 개발 입문",
                        "thumb": "https://example.com/thumb.jpg"
                    }
                    """;

        return mvc
            .perform(
                put(MENTORING_URL + "/" + mentoringId)
                    .cookie(new Cookie(TOKEN, token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req)
            )
            .andDo(print())
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("updateMentoring"));
    }

    private ResultActions performDeleteMentoring(Long mentoringId, String token) throws Exception {
        return mvc
            .perform(
                delete(MENTORING_URL + "/" + mentoringId)
                    .cookie(new Cookie(TOKEN, token))
            )
            .andDo(print())
            .andExpect(handler().handlerType(MentoringController.class))
            .andExpect(handler().methodName("deleteMentoring"));
    }
}