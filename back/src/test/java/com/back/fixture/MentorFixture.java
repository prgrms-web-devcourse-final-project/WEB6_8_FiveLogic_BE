package com.back.fixture;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import org.springframework.test.util.ReflectionTestUtils;

public class MentorFixture {

    private static final Long DEFAULT_JOB_ID = 1L;
    private static final Double DEFAULT_RATE = 4.5;
    private static final Integer DEFAULT_CAREER_YEARS = 5;

    public static Mentor create(Member member) {
        return Mentor.builder()
            .member(member)
            .jobId(DEFAULT_JOB_ID)
            .rate(DEFAULT_RATE)
            .careerYears(DEFAULT_CAREER_YEARS)
            .build();
    }

    public static Mentor create(Long id, Member member) {
        Mentor mentor = Mentor.builder()
            .member(member)
            .jobId(DEFAULT_JOB_ID)
            .rate(DEFAULT_RATE)
            .careerYears(DEFAULT_CAREER_YEARS)
            .build();

        ReflectionTestUtils.setField(mentor, "id", id);
        return mentor;
    }

    public static Mentor create(Long id, Member member, Long jobId, Double rate, Integer careerYears) {
        Mentor mentor = Mentor.builder()
            .member(member)
            .jobId(jobId)
            .rate(rate)
            .careerYears(careerYears)
            .build();

        ReflectionTestUtils.setField(mentor, "id", id);
        return mentor;
    }

    public static Mentor create() {
        Member member = MemberFixture.createDefault();
        return create(member);
    }
}
