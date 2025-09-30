package com.back.fixture.mentoring;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MentorSlotFixture {

    private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(
        LocalDate.now().plusMonths(1),
        LocalTime.of(10, 0, 0)
    );

    private static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(
        LocalDate.now().plusMonths(1),
        LocalTime.of(11, 0, 0)
    );

    public static MentorSlot create(Mentor mentor) {
        return MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(DEFAULT_START_TIME)
            .endDateTime(DEFAULT_END_TIME)
            .build();
    }

    public static MentorSlot create(Long id, Mentor mentor) {
        MentorSlot slot = MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(DEFAULT_START_TIME)
            .endDateTime(DEFAULT_END_TIME)
            .build();

        ReflectionTestUtils.setField(slot, "id", id);
        return slot;
    }

    public static MentorSlot create(Long id, Mentor mentor, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        MentorSlot slot = MentorSlot.builder()
            .mentor(mentor)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .build();

        ReflectionTestUtils.setField(slot, "id", id);
        return slot;
    }
}
