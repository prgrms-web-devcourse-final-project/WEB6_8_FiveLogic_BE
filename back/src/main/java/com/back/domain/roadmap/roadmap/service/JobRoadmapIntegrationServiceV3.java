package com.back.domain.roadmap.roadmap.service;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.repository.JobRepository;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode.RoadmapType;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapNodeStatRepository;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.RoadmapNodeRepository;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.global.exception.ServiceException;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapIntegrationServiceV3 {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final JobRepository jobRepository;
    private final JobRoadmapRepository jobRoadmapRepository;
    private final TaskRepository taskRepository;
    private final JobRoadmapNodeStatRepository jobRoadmapNodeStatRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final TextFieldIntegrationService textFieldIntegrationService;

    private final RoadmapAggregator roadmapAggregator;
    private final RoadmapTreeBuilder roadmapTreeBuilder;

    // --- 품질 필터링 상수 ---
    private static final double MIN_STANDARDIZATION_RATE = 0.5; // 최소 표준화율 50%
    private static final double MIN_QUALITY_THRESHOLD = 0.4;    // 최소 품질 점수 40%
    private static final double QUALITY_NODE_COUNT_WEIGHT = 0.3; // 품질 점수: 노드 개수 가중치
    private static final double QUALITY_STANDARDIZATION_WEIGHT = 0.7; // 품질 점수: 표준화율 가중치


    // ========================================
    // Public API
    // ========================================

    /**
     * 직업 로드맵 통합 (DB 커넥션 점유 시간 감소를 위해 AI 호출을 트랜잭션 밖에서 수행)
     *
     * 트랜잭션 전략:
     * 1. 짧은 트랜잭션으로 데이터 로드 (읽기 전용)
     * 2. 트랜잭션 외부에서 AI 호출 (DB 커넥션 미사용)
     * 3. 짧은 트랜잭션으로 기존 로드맵 삭제 + 새 로드맵 저장 (쓰기)
     */
    public JobRoadmap integrateJobRoadmap(Long jobId) {
        // 1. 데이터 로드 (짧은 읽기 전용 트랜잭션)
        IntegrationData data = loadIntegrationData(jobId);

        // 2. 메모리 집계 (트랜잭션 외부)
        RoadmapAggregator.AggregationResult aggregation = roadmapAggregator.aggregate(data.mentorRoadmaps);

        // 3. Task prefetch (트랜잭션 외부, 읽기만 수행)
        Map<Long, Task> taskMap = prefetchTasks(aggregation);

        // 4. 트리 구성 및 AI 호출 (트랜잭션 외부, 6-10분 소요)
        // 이 시간 동안 DB 커넥션은 사용하지 않음
        RoadmapTreeBuilder.TreeBuildResult treeResult = roadmapTreeBuilder.build(aggregation, taskMap);

        // 5. 로그
        logOrphanNodes(treeResult, aggregation);

        // 6. 기존 로드맵 삭제 + 새 로드맵 저장 (짧은 쓰기 트랜잭션)
        return replaceJobRoadmap(data.job, treeResult, aggregation);
    }

    /**
     * 통합에 필요한 데이터 로드 (짧은 읽기 전용 트랜잭션)
     */
    @Transactional(readOnly = true)
    public IntegrationData loadIntegrationData(Long jobId) {
        Job job = validateAndGetJob(jobId);
        List<MentorRoadmap> mentorRoadmaps = loadAndFilterMentorRoadmaps(jobId);
        return new IntegrationData(job, mentorRoadmaps);
    }

    /**
     * 기존 로드맵 삭제 후 새 로드맵 저장 (짧은 쓰기 트랜잭션)
     *
     * 트랜잭션 시간: 약 0.5초
     * - DELETE: 기존 JobRoadmap, RoadmapNode, JobRoadmapNodeStat
     * - INSERT: 새 JobRoadmap, RoadmapNode, JobRoadmapNodeStat
     * - COMMIT
     *
     * 조회 불가 구간: DELETE 실행 ~ INSERT 완료 (약 50ms, COMMIT 시점)
     */
    @Transactional
    public JobRoadmap replaceJobRoadmap(Job job, RoadmapTreeBuilder.TreeBuildResult treeResult, RoadmapAggregator.AggregationResult aggregation) {
        // 1. 기존 로드맵 삭제
        deleteExistingJobRoadmap(job);

        // 2. 새 로드맵 저장
        JobRoadmap newRoadmap = persistJobRoadmap(job, treeResult, aggregation);

        log.info("JobRoadmap 교체 완료: jobId={}, 새 로드맵 id={}", job.getId(), newRoadmap.getId());
        return newRoadmap;
    }


    // ========================================
    // Step 1: 데이터 준비 및 필터링
    // ========================================

    private Job validateAndGetJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ServiceException("404", "직업을 찾을 수 없습니다. id=" + jobId));
    }

    private void deleteExistingJobRoadmap(Job job) {
        jobRoadmapRepository.findByJob(job).ifPresent(existing -> {
            Long roadmapId = existing.getId();

            // 1. 연관된 노드 ID들 조회
            List<Long> nodeIds = roadmapNodeRepository.findIdsByRoadmapIdAndRoadmapType(
                    roadmapId,
                    RoadmapType.JOB
            );

            // 2. 해당 노드들의 JobRoadmapNodeStat 먼저 삭제
            if (!nodeIds.isEmpty()) {
                jobRoadmapNodeStatRepository.deleteByNodeIdIn(nodeIds);
            }

            // 3. 레벨이 깊은 노드부터 삭제 (leaf → root 순서)
            // 최대 레벨 조회
            Integer maxLevel = roadmapNodeRepository.findMaxLevelByRoadmapIdAndRoadmapType(
                    roadmapId, RoadmapType.JOB);

            if (maxLevel != null) {
                // 가장 깊은 레벨부터 0까지 역순으로 삭제
                for (int level = maxLevel; level >= 0; level--) {
                    roadmapNodeRepository.deleteByRoadmapIdAndRoadmapTypeAndLevel(
                            roadmapId, RoadmapType.JOB, level);
                }
            }

            // 5. JobRoadmap 삭제
            jobRoadmapRepository.delete(existing);

            log.info("기존 JobRoadmap 삭제: id={}, 노드들 먼저 삭제됨", roadmapId);
        });
    }

    private List<MentorRoadmap> loadAndFilterMentorRoadmaps(Long jobId) {
        List<MentorRoadmap> all = mentorRoadmapRepository.findAllByMentorJobIdWithNodes(jobId);

        List<MentorRoadmap> filtered = all.stream()
                .filter(mr -> mr.getNodes() != null && mr.getNodes().size() >= 3)
                .filter(mr -> calculateStandardizationRate(mr) >= MIN_STANDARDIZATION_RATE)
                .filter(mr -> calculateRoadmapQuality(mr) >= MIN_QUALITY_THRESHOLD)
                .toList();

        if (filtered.isEmpty()) {
            throw new ServiceException("404", "해당 직업에 대한 유효한 멘토 로드맵이 존재하지 않습니다. " +
                    "(최소 조건: 노드 3개 이상, 표준화율 " + (int)(MIN_STANDARDIZATION_RATE * 100) + "% 이상, 품질 점수 " + MIN_QUALITY_THRESHOLD + " 이상)");
        }

        log.info("멘토 로드맵 품질 필터링: 전체 {}개 → 유효 {}개 (노드 3개 이상, 표준화율 {}% 이상, 품질 점수 {} 이상)",
                all.size(), filtered.size(), (int)(MIN_STANDARDIZATION_RATE * 100), MIN_QUALITY_THRESHOLD);

        return filtered;
    }


    // ========================================
    // Step 2: Task Prefetch (N+1 방지)
    // ========================================

    /**
     * Task 엔티티를 일괄 로드하여 N+1 문제 방지
     * @param aggregation 집계 결과
     * @return taskId → Task 매핑
     */
    private Map<Long, Task> prefetchTasks(RoadmapAggregator.AggregationResult aggregation) {
        Set<Long> taskIds = aggregation.agg.values().stream()
                .map(a -> a.task != null ? a.task.getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Task> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            taskRepository.findAllById(taskIds).forEach(t -> taskMap.put(t.getId(), t));
        }

        return taskMap;
    }


    // ========================================
    // Step 3: 로그 및 영속화
    // ========================================

    private void logOrphanNodes(RoadmapTreeBuilder.TreeBuildResult result, RoadmapAggregator.AggregationResult aggregation) {
        long orphanCount = result.keyToNode.values().stream()
                .filter(n -> !result.visited.contains(RoadmapAggregator.generateKey(n)))
                .count();

        if (orphanCount > 0) {
            log.info("=== 제외된 고아 노드 ===");
            result.keyToNode.entrySet().stream()
                    .filter(e -> !result.visited.contains(e.getKey()))
                    .forEach(e -> {
                        String key = e.getKey();
                        RoadmapAggregator.AggregatedNode aggNode = aggregation.agg.get(key);
                        int count = aggNode != null ? aggNode.count : 0;
                        int mentorCount = aggregation.mentorAppearSet.getOrDefault(key, Collections.emptySet()).size();
                        log.info("  - 키: {}, 이름: {}, 출현빈도: {}회, 멘토수: {}명",
                                key, e.getValue().getTaskName(), count, mentorCount);
                    });
            log.info("총 {}개의 저빈도 노드가 메인 트리에서 제외되었습니다.", orphanCount);
        }
    }

    private JobRoadmap persistJobRoadmap(Job job, RoadmapTreeBuilder.TreeBuildResult treeResult, RoadmapAggregator.AggregationResult aggregation) {
        JobRoadmap jobRoadmap = jobRoadmapRepository.save(JobRoadmap.builder().job(job).build());
        Long roadmapId = jobRoadmap.getId();

        attachNodesToRoadmap(jobRoadmap, treeResult, roadmapId);
        JobRoadmap saved = jobRoadmapRepository.save(jobRoadmap);

        saveNodeStatistics(saved, treeResult, aggregation);

        log.info("JobRoadmap 생성 완료: id={}, 노드={}개", saved.getId(), saved.getNodes().size());
        return saved;
    }

    private void attachNodesToRoadmap(JobRoadmap roadmap, RoadmapTreeBuilder.TreeBuildResult treeResult, Long roadmapId) {
        List<RoadmapNode> allNodes = treeResult.keyToNode.values().stream()
                .filter(n -> treeResult.visited.contains(RoadmapAggregator.generateKey(n)))
                .peek(n -> n.assignToRoadmap(roadmapId, RoadmapType.JOB))
                .toList();

        roadmap.getNodes().addAll(allNodes);
    }

    private void saveNodeStatistics(JobRoadmap roadmap, RoadmapTreeBuilder.TreeBuildResult treeResult, RoadmapAggregator.AggregationResult aggregation) {
        List<JobRoadmapNodeStat> stats = roadmap.getNodes().stream()
                .map(node -> createNodeStat(node, treeResult, aggregation))
                .toList();

        jobRoadmapNodeStatRepository.saveAll(stats);
    }

    private JobRoadmapNodeStat createNodeStat(RoadmapNode node, RoadmapTreeBuilder.TreeBuildResult treeResult, RoadmapAggregator.AggregationResult aggregation) {
        String key = RoadmapAggregator.generateKey(node);
        RoadmapAggregator.AggregatedNode aggNode = aggregation.agg.get(key);

        int mentorCount = aggregation.mentorAppearSet.getOrDefault(key, Collections.emptySet()).size();

        List<Integer> posList = aggregation.positions.getOrDefault(key, Collections.emptyList());
        Double avgPos = posList.isEmpty() ? null : posList.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        double frequencyScore = aggNode == null ? 0.0 : (double) aggNode.count / (double) aggregation.totalMentorCount;
        double mentorCoverageScore = (double) mentorCount / (double) aggregation.totalMentorCount;
        double positionScore = avgPos != null ? 1.0 / (avgPos + 1) : 0.0;

        int outgoing = aggregation.transitions.getOrDefault(key, Collections.emptyMap()).values().stream()
                .mapToInt(Integer::intValue).sum();
        int incoming = aggregation.transitions.entrySet().stream()
                .mapToInt(e -> e.getValue().getOrDefault(key, 0)).sum();
        int totalTransitions = outgoing + incoming;
        double connectivityScore = totalTransitions > 0 ? Math.min(1.0, (double) totalTransitions / (aggregation.totalMentorCount * 2)) : 0.0;

        double weight = 0.4 * frequencyScore +
                0.3 * mentorCoverageScore +
                0.2 * positionScore +
                0.1 * connectivityScore;

        weight = Math.max(0.0, Math.min(1.0, weight));

        Map<String, Integer> outMap = aggregation.transitions.getOrDefault(key, Collections.emptyMap());
        String transitionCountsJson = null;
        if (!outMap.isEmpty()) {
            transitionCountsJson = Ut.json.toString(outMap);
        }

        List<RoadmapTreeBuilder.AlternativeParentInfo> altParents = treeResult.skippedParents.get(key);
        String alternativeParentsJson = null;
        if (altParents != null && !altParents.isEmpty()) {
            alternativeParentsJson = Ut.json.toString(altParents);
        }

        return JobRoadmapNodeStat.builder()
                .node(node)
                .stepOrder(node.getStepOrder())
                .weight(weight)
                .averagePosition(avgPos)
                .mentorCount(mentorCount)
                .totalMentorCount(aggregation.totalMentorCount)
                .mentorCoverageRatio(mentorCoverageScore)
                .outgoingTransitions(outgoing)
                .incomingTransitions(incoming)
                .transitionCounts(transitionCountsJson)
                .alternativeParents(alternativeParentsJson)
                .build();
    }


    // ========================================
    // 헬퍼 메서드
    // ========================================

    /**
     * 멘토 로드맵의 Task 표준화율 계산
     * @param roadmap 멘토 로드맵
     * @return 표준화율 (0.0 ~ 1.0)
     */
    private double calculateStandardizationRate(MentorRoadmap roadmap) {
        List<RoadmapNode> nodes = roadmap.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }

        long standardizedCount = nodes.stream()
                .filter(n -> n.getTask() != null)
                .count();

        return (double) standardizedCount / nodes.size();
    }

    /**
     * 멘토 로드맵의 품질 점수 계산
     * 품질 점수 = (노드 개수 점수 × 0.3) + (표준화율 × 0.7)
     *
     * @param roadmap 멘토 로드맵
     * @return 품질 점수 (0.0 ~ 1.0)
     */
    private double calculateRoadmapQuality(MentorRoadmap roadmap) {
        List<RoadmapNode> nodes = roadmap.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return 0.0;
        }

        // 노드 개수 점수 (3개=0.0, 15개=1.0)
        int nodeCount = nodes.size();
        double nodeScore = Math.min(1.0, (nodeCount - 3.0) / 12.0);

        // 표준화율
        double standardizationScore = calculateStandardizationRate(roadmap);

        // 복합 점수 (노드 개수 30%, 표준화율 70%)
        return QUALITY_NODE_COUNT_WEIGHT * nodeScore + QUALITY_STANDARDIZATION_WEIGHT * standardizationScore;
    }


    // ========================================
    // 데이터 전달용 DTO
    // ========================================

    /**
     * 통합에 필요한 데이터를 트랜잭션 간 전달하기 위한 컨테이너
     */
    private record IntegrationData(Job job, List<MentorRoadmap> mentorRoadmaps) {}
}
