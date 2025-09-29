package com.back.fixture.mentoring;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class MentorSlotFixture {

    private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2025, 10, 1, 14, 0);
    private static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2025, 10, 1, 16, 0);

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
