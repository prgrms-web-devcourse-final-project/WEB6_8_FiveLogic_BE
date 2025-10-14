package com.back.fixture;

import com.back.domain.job.job.entity.Job;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import org.springframework.test.util.ReflectionTestUtils;

public class MentorFixture {

    private static final Double DEFAULT_RATE = 4.5;
    private static final Integer DEFAULT_CAREER_YEARS = 5;

    public static Mentor create(Member member) {
        return Mentor.builder()
            .member(member)
            .job(JobFixture.createDefault())
            .rate(DEFAULT_RATE)
            .careerYears(DEFAULT_CAREER_YEARS)
            .build();
    }

    public static Mentor create(Long id, Member member) {
        Mentor mentor = Mentor.builder()
            .member(member)
            .job(JobFixture.createDefault())
            .rate(DEFAULT_RATE)
            .careerYears(DEFAULT_CAREER_YEARS)
            .build();

        ReflectionTestUtils.setField(mentor, "id", id);
        return mentor;
    }

    public static Mentor create(Long id, Member member, Job job, Double rate, Integer careerYears) {
        Mentor mentor = Mentor.builder()
            .member(member)
            .job(job)
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
