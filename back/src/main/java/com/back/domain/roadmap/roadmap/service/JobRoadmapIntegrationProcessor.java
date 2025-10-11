package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.entity.JobRoadmapIntegrationQueue;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapIntegrationQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapIntegrationProcessor {
    private final JobRoadmapIntegrationQueueRepository queueRepository;
    private final JobRoadmapIntegrationServiceV3 integrationService;

    /**
     * 단일 큐 항목 처리 (통합 + 큐 삭제를 하나의 트랜잭션으로)
     * REQUIRES_NEW: 각 큐 항목이 독립적인 트랜잭션
     *
     * ObjectOptimisticLockingFailureException 발생 시:
     * - 다른 트랜잭션(이벤트 리스너)이 이 큐를 동시에 수정함
     * - 최신 요청(requestedAt 갱신)이 반영되었으므로 이번 처리는 무시
     * - 전체 트랜잭션 롤백 (통합 결과도 저장 안 됨)
     * - 다음 스케줄링 때 갱신된 큐로 재처리됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processQueue(JobRoadmapIntegrationQueue queue) {
        Long jobId = queue.getJobId();

        // 1. 통합 실행
        integrationService.integrateJobRoadmap(jobId);

        // 2. 성공 시 큐 삭제 (같은 트랜잭션)
        queueRepository.delete(queue);

        log.info("직업 로드맵 통합 성공: jobId={}", jobId);
    }

    /**
     * 재시도 로직 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRetry(JobRoadmapIntegrationQueue queue, int maxRetry) {
        if (queue.isMaxRetryExceeded(maxRetry)) {
            queueRepository.delete(queue);
            log.warn("최대 재시도 횟수 초과로 큐에서 제거: jobId={}", queue.getJobId());
        } else {
            queue.incrementRetryCount();
            queue.updateRequestedAt();
            queueRepository.save(queue);
            log.info("재시도 예약: jobId={}, retryCount={}", queue.getJobId(), queue.getRetryCount());
        }
    }
}
