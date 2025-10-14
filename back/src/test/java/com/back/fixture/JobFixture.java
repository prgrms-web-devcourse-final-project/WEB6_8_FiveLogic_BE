package com.back.fixture;

import com.back.domain.job.job.entity.Job;
import org.springframework.test.util.ReflectionTestUtils;

public class JobFixture {
    private String name = "백엔드 개발자";
    private String description = "서버 사이드 로직 구현과 데이터베이스를 담당하는 개발자";
    private Long id = null;

    private static JobFixture builder() {
        return new JobFixture();
    }

    public static Job createDefault() {
        return builder().build();
    }

    public static Job create(Long id, String name, String description) {
        return builder()
            .withId(id)
            .withName(name)
            .withDescription(description)
            .build();
    }

    public static Job create(String name, String description) {
        return builder()
            .withName(name)
            .withDescription(description)
            .build();
    }

    public JobFixture withName(String name) {
        this.name = name;
        return this;
    }

    public JobFixture withDescription(String description) {
        this.description = description;
        return this;
    }

    public JobFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Job build() {
        Job job = new Job(name, description);
        if (id != null) {
            ReflectionTestUtils.setField(job, "id", id);
        }
        return job;
    }
}
