package com.back.fixture.mentoring;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.Tag;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import com.back.domain.mentoring.mentoring.repository.TagRepository;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.reservation.repository.ReservationRepository;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.domain.mentoring.slot.repository.MentorSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MentoringTestFixture {
    @Autowired private MentoringRepository mentoringRepository;
    @Autowired private MentorSlotRepository mentorSlotRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private TagRepository tagRepository;

    private int counter = 0;
    List<String> DEFAULT_TAG_NAMES = List.of("Spring", "Java");

    // ===== Mentoring =====

    public Mentoring createMentoring(Mentor mentor, String title, String bio, List<String> tagNames) {
        List<Tag> tags = getOrCreateTags(tagNames);

        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(title)
            .bio(bio)
            .build();
        mentoring.updateTags(tags);
        return mentoringRepository.save(mentoring);
    }

    public Mentoring createMentoring(Mentor mentor) {
        return createMentoring(
            mentor,
            "테스트 멘토링 " + (++counter),
            "테스트 설명",
            DEFAULT_TAG_NAMES
        );
    }

    public List<Mentoring> createMentorings(Mentor mentor, int count) {
        List<Tag> tags = getOrCreateTags(DEFAULT_TAG_NAMES);

        List<Mentoring> mentorings = IntStream.range(0, count)
            .mapToObj(i -> {
                Mentoring mentoring = Mentoring.builder()
                    .mentor(mentor)
                    .title("테스트 멘토링 " + (++counter))
                    .bio("테스트 설명")
                    .build();
                mentoring.updateTags(tags);
                return mentoring;
            })
            .toList();

        return mentoringRepository.saveAll(mentorings);
    }

    private List<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        // 기존 태그 조회
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

        Set<String> existingTagNames = existingTags.stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());

        // 신규 태그만 생성
        List<Tag> newTags = tagNames.stream()
            .filter(name -> !existingTagNames.contains(name))
            .map(name -> Tag.builder().name(name).build())
            .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
        }

        List<Tag> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);
        return allTags;
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
        LocalDateTime baseDateTime = LocalDateTime.now().plusWeeks(2).truncatedTo(ChronoUnit.SECONDS);
        return createMentorSlot(mentor,baseDateTime, baseDateTime.plusHours(1));
    }

    public List<MentorSlot> createMentorSlots(Mentor mentor, LocalDateTime baseDateTime, int days, int slots, Long minutes) {
        List<MentorSlot> mentorSlots = new ArrayList<>();

        // days 반복
        for(int day = 0; day < days; day++) {
            LocalDateTime weekStart = baseDateTime.plusDays(day).truncatedTo(ChronoUnit.SECONDS);

            // 30분 단위 슬롯
            for(int slot = 0; slot < slots; slot++) {
                LocalDateTime startDateTime = weekStart.plusMinutes(slot * minutes).truncatedTo(ChronoUnit.SECONDS);
                LocalDateTime endDateTime = startDateTime.plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);

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