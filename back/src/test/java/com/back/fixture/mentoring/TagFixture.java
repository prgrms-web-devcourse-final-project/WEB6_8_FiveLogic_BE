package com.back.fixture.mentoring;

import com.back.domain.mentoring.mentoring.entity.Tag;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class TagFixture {

    public static Tag create(String name) {
        return Tag.builder()
            .name(name)
            .build();
    }

    public static Tag create(Long id, String name) {
        Tag tag = Tag.builder()
            .name(name)
            .build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }

    public static List<Tag> createDefaultTags() {
        Tag spring = create(1L, "Spring");
        Tag java = create(2L, "Java");
        return List.of(spring, java);
    }
}
