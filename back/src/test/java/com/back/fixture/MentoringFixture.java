package com.back.fixture;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.repository.MentoringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MentoringFixture {
    @Autowired
    private MentoringRepository mentoringRepository;

    private int counter = 0;

    public Mentoring createMentoring(Mentor mentor, String title, String bio, List<String> tags) {
        Mentoring mentoring = Mentoring.builder()
            .mentor(mentor)
            .title(title)
            .bio(bio)
            .tags(tags)
            .build();
        return mentoringRepository.save(mentoring);
    }

    public Mentoring createMentoring(Mentor mentor) {
        return createMentoring(
            mentor,
            "테스트 멘토링 " + (++counter),
            "테스트 설명",
            List.of("Spring", "Java")
        );
    }
}