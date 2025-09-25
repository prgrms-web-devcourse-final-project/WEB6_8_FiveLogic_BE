package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapCreateRequest;
import com.back.domain.roadmap.roadmap.dto.request.RoadmapNodeRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapCreateResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapResponse;
import com.back.domain.roadmap.roadmap.dto.response.RoadmapNodeResponse;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorRoadmapService {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final TaskRepository taskRepository;

    // 멘토 로드맵 생성
    @Transactional
    public MentorRoadmapCreateResponse create(Long mentorId, MentorRoadmapCreateRequest request) {
        // 멘토가 이미 로드맵을 가지고 있는지 확인
        if (mentorRoadmapRepository.existsByMentorId(mentorId)) {
            throw new ServiceException("409", "이미 로드맵이 존재합니다. 멘토는 하나의 로드맵만 생성할 수 있습니다.");
        }

        // 노드가 최소 1개인지 검증
        if (request.nodes().isEmpty()) {
            throw new ServiceException("400", "적어도 하나 이상의 노드를 포함해야 로드맵을 생성할 수 있습니다."); // 수정
        }

        // Task 유효성 검증 + 캐싱 (중복 조회 방지)
        Map<Long, Task> validatedTasksMap = getValidatedTasksMap(request.nodes());

        // MentorRoadmap 생성
        MentorRoadmap mentorRoadmap = new MentorRoadmap(mentorId, request.title(), request.description());

        // 모든 노드를 생성하고 로드맵에 추가
        List<RoadmapNode> allNodes = createAllNodesWithCachedTasks(request.nodes(), validatedTasksMap);
        mentorRoadmap.addNodes(allNodes);

        // 로드맵 저장 (cascade로 노드들도 함께 저장)
        mentorRoadmap = mentorRoadmapRepository.save(mentorRoadmap);

        // 저장 후 roadmapId 업데이트 (cascade 후처리)
        mentorRoadmap.updateNodesWithRoadmapId();
        mentorRoadmapRepository.save(mentorRoadmap);

        log.info("멘토 로드맵 생성 완료 - 멘토 ID: {}, 로드맵 ID: {}, 노드 수: {} (cascade 활용)",
                 mentorId, mentorRoadmap.getId(), mentorRoadmap.getNodes().size());

        return new MentorRoadmapCreateResponse(
            mentorRoadmap.getId(),
            mentorRoadmap.getMentorId(),
            mentorRoadmap.getTitle(),
            mentorRoadmap.getDescription(),
            mentorRoadmap.getNodes().size(),
            mentorRoadmap.getCreateDate()
        );
    }

    // 멘토 ID로 멘토 로드맵 상세 조회
    @Transactional(readOnly = true)
    public MentorRoadmapResponse getByMentorId(Long mentorId) {
        // 로드맵과 노드들을 한 번에 조회 (성능 최적화)
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findByMentorIdWithNodes(mentorId)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        // 서비스에서 데이터 변환 로직 처리
        List<RoadmapNodeResponse> nodeResponses = mentorRoadmap.getNodes().stream()
                .map(node -> new RoadmapNodeResponse(
                    node.getId(),
                    node.getTask() != null ? node.getTask().getId() : null,
                    node.getRawTaskName(),
                    node.getTask() != null ? node.getTask().getName() : null,
                    node.getDescription(),
                    node.getStepOrder(),
                    node.getTask() != null
                ))
                .toList();

        return new MentorRoadmapResponse(
            mentorRoadmap.getId(),
            mentorRoadmap.getMentorId(),
            mentorRoadmap.getTitle(),
            mentorRoadmap.getDescription(),
            nodeResponses,
            mentorRoadmap.getCreateDate(),
            mentorRoadmap.getModifyDate()
        );
    }

    // 멘토 로드맵 삭제
    @Transactional
    public void delete(Long roadmapId, Long mentorId) {
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        // 권한 확인
        if (!mentorRoadmap.getMentorId().equals(mentorId)) {
            throw new ServiceException("403", "본인의 로드맵만 삭제할 수 있습니다.");
        }

        // cascade로 자동으로 관련 노드들도 함께 삭제됨
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
    private List<RoadmapNode> createAllNodesWithCachedTasks(
            List<RoadmapNodeRequest> nodeRequests,
            Map<Long, Task> tasksMap
    ) {
        List<RoadmapNode> nodes = new ArrayList<>();

        for (int i = 0; i < nodeRequests.size(); i++) {
            RoadmapNodeRequest nodeRequest = nodeRequests.get(i);

            // Task 정보는 캐싱된 맵에서 조회 (추가 쿼리 없음)
            Task task = nodeRequest.taskId() != null ? tasksMap.get(nodeRequest.taskId()) : null;
            String taskName = (task != null) ? task.getName() : nodeRequest.taskName();

            RoadmapNode node = new RoadmapNode(taskName, nodeRequest.description(), task);
            node.setRoadmapType(RoadmapNode.RoadmapType.MENTOR);
            node.setStepOrder(nodeRequest.stepOrder());

            nodes.add(node);
        }

        return nodes;
    }
}