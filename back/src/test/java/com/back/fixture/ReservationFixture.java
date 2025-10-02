package com.back.fixture;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class ReservationFixture {
    private Long id;
    private Mentoring mentoring;
    private Mentee mentee;
    private MentorSlot mentorSlot;
    private String preQuestion = "사전 질문입니다.";

    public static ReservationFixture builder() {
        return new ReservationFixture();
    }

    public static Reservation createDefault() {
        return builder().build();
    }

    public static Reservation create(Long id, Mentoring mentoring, Mentee mentee, MentorSlot mentorSlot, String preQuestion) {
        return builder()
                .withId(id)
                .withMentoring(mentoring)
                .withMentee(mentee)
                .withMentorSlot(mentorSlot)
                .withPreQuestion(preQuestion)
                .build();
    }

    public Reservation build() {
        Mentor mentor = Mentor.builder()
                .member(MemberFixture.createDefault())
                .build();

        Mentoring mentoring = this.mentoring != null ? this.mentoring : Mentoring.builder()
                .mentor(mentor)
                .title("테스트 멘토링")
                .build();

        Mentee mentee = this.mentee != null ? this.mentee : Mentee.builder()
                .member(MemberFixture.createDefault())
                .build();

        MentorSlot mentorSlot = this.mentorSlot != null ? this.mentorSlot : MentorSlot.builder()
                .mentor(mentor)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();

        Reservation reservation = new Reservation(mentoring, mentee, mentorSlot, preQuestion);
        if (id != null) {
            ReflectionTestUtils.setField(reservation, "id", id);
        }
        return reservation;
    }

    public ReservationFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public ReservationFixture withMentoring(Mentoring mentoring) {
        this.mentoring = mentoring;
        return this;
    }

    public ReservationFixture withMentee(Mentee mentee) {
        this.mentee = mentee;
        return this;
    }

    public ReservationFixture withMentorSlot(MentorSlot mentorSlot) {
        this.mentorSlot = mentorSlot;
        return this;
    }

    public ReservationFixture withPreQuestion(String preQuestion) {
        this.preQuestion = preQuestion;
        return this;
    }
}
