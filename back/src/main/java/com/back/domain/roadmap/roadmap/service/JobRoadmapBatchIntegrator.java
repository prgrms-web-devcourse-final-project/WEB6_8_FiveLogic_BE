package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapIntegrationQueue;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapIntegrationQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapBatchIntegrator {
    private final JobRoadmapIntegrationQueueRepository queueRepository;
    private final JobRoadmapIntegrationProcessor processor;
    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 60000)  // 10분
    public void integrate() {
        List<JobRoadmapIntegrationQueue> pendingQueues = queueRepository.findAllOrderByRequestedAt();

        if(pendingQueues.isEmpty()) {
            return;
        }

        log.info("직업 로드맵 배치 통합 시작: {}개 직업", pendingQueues.size());

        int successCount = 0;
        for(JobRoadmapIntegrationQueue queue : pendingQueues) {
            try {
                processor.processQueue(queue);
                successCount++;
            } catch (Exception e) {
                log.error("직업 로드맵 통합 실패: jobId={}, error={}",
                         queue.getJobId(), e.getMessage());
                try {
                    processor.handleRetry(queue, MAX_RETRY);
                } catch (Exception retryError) {
                    log.error("재시도 처리 실패: jobId={}, error={}",
                             queue.getJobId(), retryError.getMessage());
                }
            }
        }
        log.info("직업 로드맵 배치 통합 완료: 성공 {}/{}개", successCount, pendingQueues.size());
    }
}
