package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapIntegrationQueue;
import com.back.domain.roadmap.roadmap.event.MentorRoadmapChangeEvent;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapIntegrationQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapUpdateEventListener {
    private final JobRoadmapIntegrationQueueRepository jobRoadmapIntegrationQueueRepository;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requestJobRoadmapUpdate(MentorRoadmapChangeEvent event) {
        Long jobId = event.getJobId();

        // 재시도 로직 (지수 백오프)
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                JobRoadmapIntegrationQueue queue =
                        jobRoadmapIntegrationQueueRepository.findById(jobId)
                                .orElse(new JobRoadmapIntegrationQueue(jobId));

                queue.updateRequestedAt();
                jobRoadmapIntegrationQueueRepository.save(queue);

                log.info("직업 로드맵 재생성 예약 성공: jobId={}, 시도={}/{}", jobId, attempt, MAX_RETRY_ATTEMPTS);
                return;  // 성공 시 즉시 반환

            } catch (Exception e) {
                log.warn("큐 저장 실패 (시도 {}/{}): jobId={}, error={}",
                        attempt, MAX_RETRY_ATTEMPTS, jobId, e.getClass().getSimpleName());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    // 재시도 전 대기 (지수 백오프: 100ms, 200ms, 400ms)
                    try {
                        Thread.sleep(50L << attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("재시도 대기 중 인터럽트: jobId={}", jobId);
                        break;
                    }
                } else {
                    // 최종 실패 시 심각도 높은 로그 (외부 모니터링 대상)
                    log.error("CRITICAL: 큐 저장 최종 실패 ({}회 시도): jobId={}, error={}",
                            MAX_RETRY_ATTEMPTS, jobId, e.getMessage(), e);
                    // TODO: Sentry, Slack 등 외부 모니터링 시스템에 알림
                }
            }
        }
    }
}
