package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapSaveRequest;
import com.back.domain.roadmap.roadmap.dto.request.RoadmapNodeRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapSaveResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapResponse;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
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
    private final TaskRepository taskRepository;

    // 멘토 로드맵 생성
    @Transactional
    public MentorRoadmapSaveResponse create(Long mentorId, MentorRoadmapSaveRequest request) {
        // 멘토가 이미 로드맵을 가지고 있는지 확인
        if (mentorRoadmapRepository.existsByMentorId(mentorId)) {
            throw new ServiceException("409", "이미 로드맵이 존재합니다. 멘토는 하나의 로드맵만 생성할 수 있습니다.");
        }

        // 공통 검증
        validateRequest(request);

        // MentorRoadmap 생성 및 저장 (로드맵 ID 확보)
        MentorRoadmap mentorRoadmap = new MentorRoadmap(mentorId, request.title(), request.description());
        mentorRoadmap = mentorRoadmapRepository.save(mentorRoadmap);

        // roadmapId를 포함한 노드 생성 및 추가
        List<RoadmapNode> allNodes = createValidatedNodesWithRoadmapId(request.nodes(), mentorRoadmap.getId());
        mentorRoadmap.addNodes(allNodes);

        // 최종 저장 (노드들 CASCADE INSERT)
        mentorRoadmap = saveRoadmap(mentorRoadmap);

        log.info("멘토 로드맵 생성 완료 - 멘토 ID: {}, 로드맵 ID: {}, 노드 수: {} (cascade 활용)",
                 mentorId, mentorRoadmap.getId(), mentorRoadmap.getNodes().size());

        return new MentorRoadmapSaveResponse(
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
    public MentorRoadmapResponse getById(Long id) {
        // 로드맵과 노드들을 한 번에 조회 (성능 최적화)
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findByIdWithNodes(id)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        return MentorRoadmapResponse.from(mentorRoadmap);
    }

    // 멘토 로드맵 수정
    @Transactional
    public MentorRoadmapSaveResponse update(Long id, Long mentorId, MentorRoadmapSaveRequest request) {
        // 수정하려는 로드맵이 실제로 있는지 확인
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findByIdWithNodes(id)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        // 권한 확인 - 본인의 로드맵만 수정 가능
        if (!mentorRoadmap.getMentorId().equals(mentorId)) {
            throw new ServiceException("403", "본인의 로드맵만 수정할 수 있습니다.");
        }

        // 공통 검증
        validateRequest(request);

        // 로드맵 기본 정보 수정
        mentorRoadmap.updateTitle(request.title());
        mentorRoadmap.updateDescription(request.description());

        // 기존 노드 제거 후 roadmapId를 포함한 새 노드들 추가
        mentorRoadmap.clearNodes();
        List<RoadmapNode> allNodes = createValidatedNodesWithRoadmapId(request.nodes(), mentorRoadmap.getId());
        mentorRoadmap.addNodes(allNodes);

        // 최종 저장 (노드들 CASCADE INSERT)
        mentorRoadmap = saveRoadmap(mentorRoadmap);

        log.info("멘토 로드맵 수정 완료 - 로드맵 ID: {}, 노드 수: {} (cascade 활용)",
                mentorRoadmap.getId(), mentorRoadmap.getNodes().size());

        return new MentorRoadmapSaveResponse(
                mentorRoadmap.getId(),
                mentorRoadmap.getMentorId(),
                mentorRoadmap.getTitle(),
                mentorRoadmap.getDescription(),
                mentorRoadmap.getNodes().size(),
                mentorRoadmap.getCreateDate()
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

    // 로드맵 요청 공통 유효성 검증
    private void validateRequest(MentorRoadmapSaveRequest request) {
        if (request.nodes().isEmpty()) {
            throw new ServiceException("400", "로드맵은 적어도 하나 이상의 노드를 포함해야 합니다.");
        }
        validateStepOrderSequence(request.nodes());
    }

    // stepOrder 연속성 검증 (멘토 로드맵은 선형 구조)
    private void validateStepOrderSequence(List<RoadmapNodeRequest> nodes) {
        List<Integer> stepOrders = nodes.stream()
                .map(RoadmapNodeRequest::stepOrder)
                .toList();

        // 중복 검증 먼저 수행
        long distinctCount = stepOrders.stream().distinct().count();
        if (distinctCount != stepOrders.size()) {
            throw new ServiceException("400", "stepOrder에 중복된 값이 있습니다.");
        }

        // 정렬 후 연속성 검증
        List<Integer> sortedStepOrders = stepOrders.stream().sorted().toList();

        // 1부터 시작하는 연속된 숫자인지 검증
        for (int i = 0; i < sortedStepOrders.size(); i++) {
            int expectedOrder = i + 1;
            if (!sortedStepOrders.get(i).equals(expectedOrder)) {
                throw new ServiceException("400",
                    String.format("stepOrder는 1부터 시작하는 연속된 숫자여야 합니다. 현재: %s, 기대값: %d",
                        sortedStepOrders, expectedOrder));
            }
        }
    }

    // 로드맵 저장 (노드들도 cascade로 함께 저장)
    private MentorRoadmap saveRoadmap(MentorRoadmap mentorRoadmap) {
        // 노드에 roadmapId가 이미 설정된 상태로 한 번만 저장
        return mentorRoadmapRepository.save(mentorRoadmap);
    }


    // Task 유효성 검증 후 RoadmapNode 리스트 생성 (roadmapId 포함)
    private List<RoadmapNode> createValidatedNodesWithRoadmapId(List<RoadmapNodeRequest> nodeRequests, Long roadmapId) {
        // Task 유효성 검증 + 캐싱 (중복 조회 방지)
        Map<Long, Task> validatedTasksMap = getValidatedTasksMap(nodeRequests);

        // roadmapId를 포함하여 노드 생성
        return createAllNodesWithRoadmapId(nodeRequests, validatedTasksMap, roadmapId);
    }

    // Task 유효성 검증 + 결과 캐싱 (중복 조회 방지)
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


    // 캐싱된 Task 정보를 활용하여 모든 노드 생성 (roadmapId 포함)
    private List<RoadmapNode> createAllNodesWithRoadmapId(
            List<RoadmapNodeRequest> nodeRequests,
            Map<Long, Task> tasksMap,
            Long roadmapId
    ) {
        List<RoadmapNode> nodes = new ArrayList<>();

        for (int i = 0; i < nodeRequests.size(); i++) {
            RoadmapNodeRequest nodeRequest = nodeRequests.get(i);

            // Task 정보는 캐싱된 맵에서 조회 (추가 쿼리 없음)
            Task task = nodeRequest.taskId() != null ? tasksMap.get(nodeRequest.taskId()) : null;
            String taskName = (task != null) ? task.getName() : nodeRequest.taskName();

            RoadmapNode node = RoadmapNode.builder()
                    .taskName(taskName)
                    .description(nodeRequest.description())
                    .task(task)
                    .stepOrder(nodeRequest.stepOrder())
                    .roadmapId(roadmapId)
                    .roadmapType(RoadmapNode.RoadmapType.MENTOR)
                    .build();

            nodes.add(node);
        }

        return nodes;
    }
}