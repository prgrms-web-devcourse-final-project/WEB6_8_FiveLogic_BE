package com.back.fixture.mentoring;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.MentorFixture;
import org.springframework.test.util.ReflectionTestUtils;

public class ReservationFixture {

    private static final String DEFAULT_PRE_QUESTION = "테스트 사전 질문입니다.";

    public static Reservation create(Mentoring mentoring, Mentee mentee, MentorSlot mentorSlot) {
        return Reservation.builder()
            .mentoring(mentoring)
            .mentee(mentee)
            .mentorSlot(mentorSlot)
            .preQuestion(DEFAULT_PRE_QUESTION)
            .build();
    }

    public static Reservation create(Long id, Mentoring mentoring, Mentee mentee, MentorSlot mentorSlot) {
        Reservation reservation = Reservation.builder()
            .mentoring(mentoring)
            .mentee(mentee)
            .mentorSlot(mentorSlot)
            .preQuestion(DEFAULT_PRE_QUESTION)
            .build();

        ReflectionTestUtils.setField(reservation, "id", id);

        // 양방향 연결 설정
        mentorSlot.setReservation(reservation);

        return reservation;
    }

    public static Reservation createDefault() {
        Member mentorMember = MemberFixture.createDefault();
        Member menteeMember = MemberFixture.createDefault();
        Mentor mentor = MentorFixture.create(mentorMember);
        Mentee mentee = MenteeFixture.create(menteeMember);
        Mentoring mentoring = MentoringFixture.create(mentor);
        MentorSlot mentorSlot = MentorSlotFixture.create(mentor);
        return create(mentoring, mentee, mentorSlot);
    }
}
