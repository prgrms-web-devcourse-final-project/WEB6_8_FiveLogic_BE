package com.back.fixture;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class MentoringFixture {
    @Autowired private MentoringRepository mentoringRepository;
    @Autowired private MentorSlotRepository mentorSlotRepository;
    @Autowired private ReservationRepository reservationRepository;

    private int counter = 0;

    // ===== Mentoring =====

    public Mentoring createMentoring(Mentor mentor, String title, String bio, List<String> tags) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(title)
            .bio(bio)
            .tags(tags)
            .build();
        return mentoringRepository.save(mentoring);
    }

    public Mentoring createMentoring(Mentor mentor) {
        return createMentoring(
            mentor,
            "테스트 멘토링 " + (++counter),
            "테스트 설명",
            List.of("Spring", "Java")
        );
    }

    public List<Mentoring> createMentorings(Mentor mentor, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createMentoring(mentor))
            .toList();
    }


    // ===== MentorSlot =====

    public MentorSlot createMentorSlot(Mentor mentor, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        MentorSlot mentorSlot = MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .build();
        return mentorSlotRepository.save(mentorSlot);
    }

    public MentorSlot createMentorSlot(Mentor mentor) {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 9, 30, 10, 0);
        return createMentorSlot(mentor,baseDateTime, baseDateTime.minusHours(1));
    }

    public List<MentorSlot> createMentorSlots(Mentor mentor, int days, int slots) {
        LocalDateTime baseDateTime = LocalDateTime.of(2025, 9, 30, 10, 0);
        List<MentorSlot> mentorSlots = new ArrayList<>();

        // days 반복
        for(int day = 0; day < days; day++) {
            LocalDateTime weekStart = baseDateTime.plusDays(day);

            // 30분 단위 슬롯
            for(int slot = 0; slot < slots; slot++) {
                LocalDateTime startDateTime = weekStart.plusMinutes(slot * 30L);
                LocalDateTime endDateTime = startDateTime.plusMinutes(30L);

                MentorSlot mentorSlot = MentorSlot.builder()
                    .mentor(mentor)
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime)
                    .build();
                mentorSlots.add(mentorSlot);
            }
        }
        return mentorSlotRepository.saveAll(mentorSlots);
    }


    // ===== Reservation =====

    public Reservation createReservation(Mentoring mentoring, Mentee mentee, MentorSlot slot, String preQuestion) {
        Reservation reservation = Reservation.builder()
            .mentoring(mentoring)
            .mentee(mentee)
            .mentorSlot(slot)
            .preQuestion(preQuestion)
            .build();
        return reservationRepository.save(reservation);
    }

    public Reservation createReservation(Mentoring mentoring, Mentee mentee, MentorSlot slot) {
        return createReservation(mentoring, mentee, slot, "멘토링 전 질문 사항");
    }
}