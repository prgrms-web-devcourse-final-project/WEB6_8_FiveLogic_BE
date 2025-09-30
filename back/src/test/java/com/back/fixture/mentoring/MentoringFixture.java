package com.back.fixture.mentoring;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class MentoringFixture {

    private static final String DEFAULT_TITLE = "테스트 멘토링";
    private static final String DEFAULT_BIO = "테스트 설명";
    private static final List<String> DEFAULT_TAGS = List.of("Spring", "Java");
    private static final String DEFAULT_THUMB = "https://example.com/thumb.jpg";

    public static Mentoring create(Mentor mentor) {
        return Mentoring.builder()
            .mentor(mentor)
            .title(DEFAULT_TITLE)
            .bio(DEFAULT_BIO)
            .tags(DEFAULT_TAGS)
            .thumb(DEFAULT_THUMB)
            .build();
    }

    public static Mentoring create(Long id, Mentor mentor) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(DEFAULT_TITLE)
            .bio(DEFAULT_BIO)
            .tags(DEFAULT_TAGS)
            .thumb(DEFAULT_THUMB)
            .build();

        ReflectionTestUtils.setField(mentoring, "id", id);
        return mentoring;
    }

    public static Mentoring create(Long id, Mentor mentor, String title, String bio, List<String> tags) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(title)
            .bio(bio)
            .tags(tags)
            .thumb(DEFAULT_THUMB)
            .build();

        if (id != null) {
            ReflectionTestUtils.setField(mentoring, "id", id);
        }
        return mentoring;
    }
}
