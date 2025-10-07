package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapIntegrationQueue;
import com.back.domain.roadmap.roadmap.event.MentorRoadmapSaveEvent;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapIntegrationQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapUpdateEventListener {
    private final JobRoadmapIntegrationQueueRepository jobRoadmapIntegrationQueueRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void requestJobRoadmapUpdate(MentorRoadmapSaveEvent event) {
        Long jobId = event.getJobId();

        JobRoadmapIntegrationQueue queue =
                jobRoadmapIntegrationQueueRepository.findById(jobId)
                        .orElse(JobRoadmapIntegrationQueue.builder()
                                .jobId(jobId)
                                .build());

        queue.updateRequestedAt();
        jobRoadmapIntegrationQueueRepository.save(queue);
        log.info("직업 로드맵 재생성 예약: jobId: {}", jobId);
    }
}
