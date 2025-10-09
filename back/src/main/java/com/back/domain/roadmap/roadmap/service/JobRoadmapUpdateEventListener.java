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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requestJobRoadmapUpdate(MentorRoadmapChangeEvent event) {
        Long jobId = event.getJobId();

        try {
            JobRoadmapIntegrationQueue queue =
                    jobRoadmapIntegrationQueueRepository.findById(jobId)
                            .orElse(new JobRoadmapIntegrationQueue(jobId));

            queue.updateRequestedAt();
            jobRoadmapIntegrationQueueRepository.save(queue);
            log.info("직업 로드맵 재생성 예약: jobId: {}", jobId);

        } catch (Exception e) {
            log.error("큐 저장 실패: jobId={}, error={}", event.getJobId(), e.getMessage());
            // 재발행 또는 별도 에러 큐 저장
        }
    }
}
