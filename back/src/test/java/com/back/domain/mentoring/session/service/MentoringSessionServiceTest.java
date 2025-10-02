package com.back.domain.mentoring.session.service;

import com.back.domain.mentoring.session.repository.MentoringSessionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MentoringSessionServiceTest {
    @Mock
    private MentoringSessionRepository mentoringSessionRepository;
    @InjectMocks
    private MentoringSessionService mentoringSessionService;

    @Test
    @DisplayName("멘토링 세션이 생성된다." )
    void createMentoringSession() {
        Assertions.fail();
    }

    @Test
    @DisplayName("예약이 승인되지 않은 멘토링은 세션을 생성할 수 없다.")
    void cannotCreateMentoringSessionIfNotApproved() {
        Assertions.fail();
    }

    @Test
    @DisplayName("멘토링 세션이 조회된다.")
    void getMentoringSession() {
        Assertions.fail();
    }

    @Test
    @DisplayName("존재하지 않는 멘토링 세션은 조회하면 예외가 발생한다.")
    void cannotGetNonExistentMentoringSession() {
        Assertions.fail();
    }
}
