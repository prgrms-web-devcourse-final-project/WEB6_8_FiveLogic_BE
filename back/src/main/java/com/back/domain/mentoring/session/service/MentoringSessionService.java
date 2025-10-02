package com.back.domain.mentoring.session.service;

import com.back.domain.mentoring.session.repository.MentoringSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentoringSessionService {
    private final MentoringSessionRepository mentoringSessionRepository;
}
