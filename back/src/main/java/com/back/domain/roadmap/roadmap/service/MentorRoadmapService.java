package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapCreateRequest;
import com.back.domain.roadmap.roadmap.dto.request.RoadmapNodeRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapCreateResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapResponse;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.RoadmapNodeRepository;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorRoadmapService {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final TaskRepository taskRepository;

    /**
     * 멘토 로드맵 생성 (성능 최적화됨)
     * - Task 중복 조회 제거
     * - 일괄 처리로 쿼리 최소화
     * - 트랜잭션 내에서 일관성 보장
     */
    @Transactional
    public MentorRoadmapCreateResponse create(Long mentorId, MentorRoadmapCreateRequest request) {
        // 1. 멘토가 이미 로드맵을 가지고 있는지 확인
        if (mentorRoadmapRepository.existsByMentorId(mentorId)) {
            throw new ServiceException("409", "이미 로드맵이 존재합니다. 멘토는 하나의 로드맵만 생성할 수 있습니다.");
        }

        // 2. Task 유효성 검증 + 캐싱 (중복 조회 방지)
        Map<Long, Task> validatedTasksMap = getValidatedTasksMap(request.nodes());

        // 3. 모든 노드를 미리 생성 (캐싱된 Task 정보 활용)
        List<RoadmapNode> allNodes = createAllNodesWithCachedTasks(request.nodes(), validatedTasksMap);

        // 4. 첫 번째 노드를 rootNode로 설정
        RoadmapNode rootNode = allNodes.get(0);

        // 5. MentorRoadmap 생성 및 저장
        MentorRoadmap mentorRoadmap = new MentorRoadmap(mentorId, request.title(), request.description(), rootNode);
        mentorRoadmap = mentorRoadmapRepository.save(mentorRoadmap);

        // 6. 모든 노드에 roadmapId 설정 후 일괄 저장
        Long roadmapId = mentorRoadmap.getId(); // effectively final 변수로 추출
        allNodes.forEach(node -> node.setRoadmapId(roadmapId));

        roadmapNodeRepository.saveAll(allNodes);

        log.info("멘토 로드맵 생성 완료 - 멘토 ID: {}, 로드맵 ID: {}, 노드 수: {} (쿼리 최적화 적용)",
                 mentorId, mentorRoadmap.getId(), request.nodes().size());

        return new MentorRoadmapCreateResponse(mentorRoadmap, request.nodes().size());
    }

    /**
     * 멘토 로드맵 상세 조회
     * - 단일 쿼리로 모든 노드 조회 (fetch join)
     * - stepOrder 순으로 정렬
     */
    @Transactional(readOnly = true)
    public MentorRoadmapResponse getById(Long roadmapId) {
        // 1. 로드맵 기본 정보 조회
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        // 2. 모든 노드를 단일 쿼리로 조회 (N+1 문제 방지)
        List<RoadmapNode> nodes = roadmapNodeRepository.findMentorRoadmapNodesWithTask(roadmapId);

        if (nodes.isEmpty()) {
            throw new ServiceException("404", "로드맵 노드를 찾을 수 없습니다.");
        }

        return new MentorRoadmapResponse(mentorRoadmap, nodes);
    }

    /**
     * 멘토의 로드맵 조회 (멘토 ID로)
     */
    @Transactional(readOnly = true)
    public Optional<MentorRoadmapResponse> getByMentorId(Long mentorId) {
        Optional<MentorRoadmap> mentorRoadmapOpt = mentorRoadmapRepository.findByMentorId(mentorId);

        if (mentorRoadmapOpt.isEmpty()) {
            return Optional.empty();
        }

        MentorRoadmap mentorRoadmap = mentorRoadmapOpt.get();
        List<RoadmapNode> nodes = roadmapNodeRepository.findMentorRoadmapNodesWithTask(mentorRoadmap.getId());

        return Optional.of(new MentorRoadmapResponse(mentorRoadmap, nodes));
    }

    /**
     * 멘토 로드맵 삭제
     * - cascade로 노드들 자동 삭제
     */
    @Transactional
    public void delete(Long roadmapId, Long mentorId) {
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        // 권한 확인
        if (!mentorRoadmap.getMentorId().equals(mentorId)) {
            throw new ServiceException("403", "본인의 로드맵만 삭제할 수 있습니다.");
        }

        // 관련 노드들 먼저 삭제
        roadmapNodeRepository.deleteByRoadmapIdAndRoadmapType(roadmapId, RoadmapNode.RoadmapType.MENTOR);

        // 로드맵 삭제
        mentorRoadmapRepository.delete(mentorRoadmap);

        log.info("멘토 로드맵 삭제 완료 - 멘토 ID: {}, 로드맵 ID: {}", mentorId, roadmapId);
    }

    /**
     * Task 유효성 검증 + 결과 캐싱 (중복 조회 방지)
     * @param nodeRequests 노드 요청 목록
     * @return taskId -> Task 매핑 맵 (null taskId 제외)
     */
    private Map<Long, Task> getValidatedTasksMap(List<RoadmapNodeRequest> nodeRequests) {
        List<Long> taskIds = nodeRequests.stream()
                .map(RoadmapNodeRequest::taskId)
                .filter(taskId -> taskId != null)
                .distinct()
                .toList();

        if (taskIds.isEmpty()) {
            return Map.of(); // 빈 맵 반환
        }

        // 일괄 조회로 존재하는 Task들 확인
        List<Task> existingTasks = taskRepository.findAllById(taskIds);
        Map<Long, Task> existingTaskMap = existingTasks.stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        // 존재하지 않는 TaskId 확인
        List<Long> missingTaskIds = taskIds.stream()
                .filter(taskId -> !existingTaskMap.containsKey(taskId))
                .toList();

        if (!missingTaskIds.isEmpty()) {
            throw new ServiceException("404",
                    String.format("존재하지 않는 Task ID: %s", missingTaskIds));
        }

        return existingTaskMap;
    }

    /**
     * 캐싱된 Task 정보를 활용하여 모든 노드 생성 (중복 조회 방지)
     * @param nodeRequests 노드 요청 목록
     * @param tasksMap 캐싱된 Task 정보
     * @return 생성된 RoadmapNode 목록
     */
    private List<RoadmapNode> createAllNodesWithCachedTasks(List<RoadmapNodeRequest> nodeRequests,
                                                           Map<Long, Task> tasksMap) {
        List<RoadmapNode> nodes = new ArrayList<>();

        for (int i = 0; i < nodeRequests.size(); i++) {
            RoadmapNodeRequest nodeRequest = nodeRequests.get(i);

            // Task 정보는 캐싱된 맵에서 조회 (추가 쿼리 없음)
            Task task = nodeRequest.taskId() != null ? tasksMap.get(nodeRequest.taskId()) : null;

            RoadmapNode node = new RoadmapNode(nodeRequest.taskName(), nodeRequest.description(), task);
            node.setRoadmapType(RoadmapNode.RoadmapType.MENTOR);
            node.setStepOrder(i + 1);

            nodes.add(node);
        }

        return nodes;
    }
}