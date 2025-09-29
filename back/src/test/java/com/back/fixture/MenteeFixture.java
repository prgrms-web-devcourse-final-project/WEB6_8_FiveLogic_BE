package com.back.fixture;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import org.springframework.test.util.ReflectionTestUtils;

public class MenteeFixture {

    private static final Long DEFAULT_JOB_ID = 1L;

    public static Mentee create(Member member) {
        return Mentee.builder()
            .member(member)
            .jobId(DEFAULT_JOB_ID)
            .build();
    }

    public static Mentee create(Long id, Member member) {
        Mentee mentee = Mentee.builder()
            .member(member)
            .jobId(DEFAULT_JOB_ID)
            .build();

        ReflectionTestUtils.setField(mentee, "id", id);
        return mentee;
    }

    public static Mentee create(Long id, Member member, Long jobId) {
        Mentee mentee = Mentee.builder()
            .member(member)
            .jobId(jobId)
            .build();

        ReflectionTestUtils.setField(mentee, "id", id);
        return mentee;
    }
}
