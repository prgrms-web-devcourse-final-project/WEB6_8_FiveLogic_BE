package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapIntegrationQueue;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapIntegrationQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @Scheduled(fixedDelay = 3600000)  // 1시간 (3600000ms)
    public void integrate() {
        List<JobRoadmapIntegrationQueue> pendingQueues = queueRepository.findAllOrderByRequestedAt();

        if(pendingQueues.isEmpty()) {
            log.debug("처리할 큐가 없습니다.");
            return;
        }

        log.info("직업 로드맵 배치 통합 시작: {}개 직업", pendingQueues.size());

        int successCount = 0;
        int conflictCount = 0;

        for(JobRoadmapIntegrationQueue queue : pendingQueues) {
            try {
                processor.processQueue(queue);
                successCount++;

            } catch (ObjectOptimisticLockingFailureException e) {
                // 낙관적 락 충돌: 다른 트랜잭션이 큐를 수정함 (정상 동작)
                conflictCount++;
                log.info("버전 충돌 발생 (정상): jobId={}, 다음 주기에 재처리",
                        queue.getJobId());

            } catch (Exception e) {
                // 실제 에러: 통합 로직 실패 등
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

        int failureCount = pendingQueues.size() - successCount - conflictCount;
        log.info("직업 로드맵 배치 통합 완료: 성공 {}, 충돌 {}, 실패 {}, 총 {}개",
                successCount, conflictCount, failureCount, pendingQueues.size());
    }
}
