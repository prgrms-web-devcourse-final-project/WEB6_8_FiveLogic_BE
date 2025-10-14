package com.back.fixture;

import com.back.domain.job.job.entity.Job;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import org.springframework.test.util.ReflectionTestUtils;

public class MenteeFixture {

    public static Mentee create(Member member) {
        return Mentee.builder()
            .member(member)
            .job(JobFixture.createDefault())
            .build();
    }

    public static Mentee create(Long id, Member member) {
        Mentee mentee = Mentee.builder()
            .member(member)
            .job(JobFixture.createDefault())
            .build();

        ReflectionTestUtils.setField(mentee, "id", id);
        return mentee;
    }

    public static Mentee create(Long id, Member member, Job job) {
        Mentee mentee = Mentee.builder()
            .member(member)
            .job(job)
            .build();

        ReflectionTestUtils.setField(mentee, "id", id);
        return mentee;
    }
}
