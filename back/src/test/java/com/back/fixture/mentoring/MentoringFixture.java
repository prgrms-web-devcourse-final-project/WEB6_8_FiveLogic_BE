package com.back.fixture.mentoring;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.Tag;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class MentoringFixture {

    private static final String DEFAULT_TITLE = "테스트 멘토링";
    private static final String DEFAULT_BIO = "테스트 설명";
    private static final List<Tag> DEFAULT_TAGS = List.of(new Tag("Spring"), new Tag("Java"));
    private static final String DEFAULT_THUMB = "https://example.com/thumb.jpg";

    public static Mentoring create(Mentor mentor) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(DEFAULT_TITLE)
            .bio(DEFAULT_BIO)
            .thumb(DEFAULT_THUMB)
            .build();
        mentoring.updateTags(DEFAULT_TAGS);
        return mentoring;
    }

    public static Mentoring create(Long id, Mentor mentor) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(DEFAULT_TITLE)
            .bio(DEFAULT_BIO)
            .thumb(DEFAULT_THUMB)
            .build();
        mentoring.updateTags(DEFAULT_TAGS);

        ReflectionTestUtils.setField(mentoring, "id", id);
        return mentoring;
    }

    public static Mentoring create(Long id, Mentor mentor, String title, String bio, List<String> tags) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(DEFAULT_TITLE)
            .bio(DEFAULT_BIO)
            .thumb(DEFAULT_THUMB)
            .build();
        mentoring.updateTags(DEFAULT_TAGS);

        if (id != null) {
            ReflectionTestUtils.setField(mentoring, "id", id);
        }
        return mentoring;
    }
}
