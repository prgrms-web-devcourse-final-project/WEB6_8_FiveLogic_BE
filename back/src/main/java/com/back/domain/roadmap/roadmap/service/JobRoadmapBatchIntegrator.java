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
    private final JobRoadmapIntegrationServiceV2 integrationService;
    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 600000)  // 10분
    public void integrate() {
        List<JobRoadmapIntegrationQueue> pendingQueues = queueRepository.findAll();

        if(pendingQueues.isEmpty()) {
            return;
        }

        log.info("직업 로드맵 배치 통합 시작: {}개 직업", pendingQueues.size());

        int successCount = 0;
        for(JobRoadmapIntegrationQueue queue : pendingQueues) {
            Long jobId = queue.getJobId();
            try {
                integrationService.integrateJobRoadmap(jobId);
                queueRepository.delete(queue);
                successCount++;
                log.info("직업 로드맵 통합 성공: jobId={}", jobId);
            } catch (Exception e) {
                log.error("직업 로드맵 통합 실패: jobId={}, error={}", jobId, e.getMessage());

                if(queue.isMaxRetryExceeded(MAX_RETRY)) {
                    queueRepository.delete(queue);
                    log.warn("최대 재시도 횟수 초과로 큐에서 제거: jobId={}", jobId);
                } else {
                    queue.incrementRetryCount();
                    queueRepository.save(queue);
                }
            }
        }
        log.info("직업 로드맵 배치 통합 완료: 성공 {}/{}개", successCount, pendingQueues.size());
    }
}
