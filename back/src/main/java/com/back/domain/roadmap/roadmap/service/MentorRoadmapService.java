package com.back.domain.roadmap.roadmap.service;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapSaveRequest;
import com.back.domain.roadmap.roadmap.dto.request.RoadmapNodeRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapListResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapSaveResponse;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.event.MentorRoadmapChangeEvent;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.RoadmapNodeRepository;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.service.TaskService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorRoadmapService {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final MentorRepository mentorRepository;
    private final TaskService taskService;
    private final ApplicationEventPublisher eventPublisher;

    // 멘토 로드맵 생성
    @Transactional
    public MentorRoadmapSaveResponse create(Long mentorId, MentorRoadmapSaveRequest request) {
        // 멘토 존재 확인
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new ServiceException("404", "멘토를 찾을 수 없습니다."));

        // 멘토가 이미 로드맵을 가지고 있는지 확인
        if (mentorRoadmapRepository.existsByMentor(mentor)) {
            throw new ServiceException("409", "이미 로드맵이 존재합니다. 멘토는 하나의 로드맵만 생성할 수 있습니다.");
        }

        // 공통 검증(노드 개수, stepOrder 연속성)
        validateRequest(request);

        // taskId가 null인 자유입력 Task를 자동으로 pending alias로 등록
        registerPendingAliasesForFreeInputTasks(request.nodes());

        // MentorRoadmap 생성 및 저장 (로드맵 ID 확보)
        MentorRoadmap mentorRoadmap = new MentorRoadmap(mentor, request.title(), request.description());
        mentorRoadmap = mentorRoadmapRepository.save(mentorRoadmap);

        // roadmapId를 포함한 노드 생성 및 추가
        List<RoadmapNode> allNodes = createValidatedNodesWithRoadmapId(request.nodes(), mentorRoadmap.getId());
        // CASCADE로 노드들이 자동 저장됨 (추가 save() 호출 불필요)
        mentorRoadmap.addNodes(allNodes);

        eventPublisher.publishEvent(new MentorRoadmapChangeEvent(mentor.getJobId()));

        log.info("멘토 로드맵 생성 완료 - 멘토 ID: {}, 로드맵 ID: {}, 노드 수: {} (cascade 활용)",
                 mentorId, mentorRoadmap.getId(), mentorRoadmap.getNodes().size());

        return new MentorRoadmapSaveResponse(
            mentorRoadmap.getId(),
            mentorRoadmap.getMentor().getId(),
            mentorRoadmap.getTitle(),
            mentorRoadmap.getDescription(),
            mentorRoadmap.getNodes().size(),
            mentorRoadmap.getCreateDate()
        );
    }

    // 로드맵 ID로 멘토 로드맵 상세 조회
    @Transactional(readOnly = true)
    public MentorRoadmapResponse getById(Long id) {
        // 로드맵과 노드들을 한 번에 조회
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findByIdWithNodes(id)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        return MentorRoadmapResponse.from(mentorRoadmap);
    }

    // 멘토 ID로 멘토 로드맵 상세 조회
    @Transactional(readOnly = true)
    public MentorRoadmapResponse getByMentorId(Long mentorId) {
        // 멘토 ID로 로드맵과 노드들을 한 번에 조회
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findByMentorIdWithNodes(mentorId)
                .orElseThrow(() -> new ServiceException("404", "해당 멘토의 로드맵을 찾을 수 없습니다."));

        return MentorRoadmapResponse.from(mentorRoadmap);
    }

    // 멘토 로드맵 목록 조회 (페이징, 키워드 검색)
    @Transactional(readOnly = true)
    public Page<MentorRoadmapListResponse> getAllMentorRoadmaps(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<MentorRoadmap> mentorRoadmapPage = mentorRoadmapRepository.findAllWithKeyword(keyword, pageable);

        // MentorRoadmap -> MentorRoadmapListResponse 변환
        return mentorRoadmapPage.map(mr -> MentorRoadmapListResponse.of(
                mr.getId(),
                mr.getTitle(),
                mr.getDescription(),
                mr.getMentor().getId(),
                mr.getMentor().getMember().getId(),
                mr.getMentor().getMember().getNickname()
        ));
    }

    // 멘토 로드맵 수정
    @Transactional
    public MentorRoadmapSaveResponse update(Long id, Long mentorId, MentorRoadmapSaveRequest request) {
        // 수정하려는 로드맵이 실제로 있는지 확인
        MentorRoadmap mentorRoadmap = mentorRoadmapRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404", "로드맵을 찾을 수 없습니다."));

        // 권한 확인 - 본인의 로드맵만 수정 가능
        if (!mentorRoadmap.getMentor().getId().equals(mentorId)) {
            throw new ServiceException("403", "본인의 로드맵만 수정할 수 있습니다.");
        }

        // 공통 검증
        validateRequest(request);

        // taskId가 null인 자유입력 Task를 자동으로 pending alias로 등록
        registerPendingAliasesForFreeInputTasks(request.nodes());

        // 로드맵 기본 정보 수정
        mentorRoadmap.updateTitle(request.title());
        mentorRoadmap.updateDescription(request.description());

        // 1. 기존 노드들을 DB에서 직접 삭제
        roadmapNodeRepository.deleteByRoadmapIdAndRoadmapType(
            mentorRoadmap.getId(),
            RoadmapNode.RoadmapType.MENTOR
        );

        // 2. 새 노드들 생성 및 추가
        List<RoadmapNode> allNodes = createValidatedNodesWithRoadmapId(request.nodes(), mentorRoadmap.getId());
        mentorRoadmap.addNodes(allNodes);

        // 최종 저장 (노드들 CASCADE INSERT)
        mentorRoadmap = mentorRoadmapRepository.save(mentorRoadmap);

        log.info("멘토 로드맵 수정 완료 - 로드맵 ID: {}, 노드 수: {} (cascade 활용)",
                mentorRoadmap.getId(), mentorRoadmap.getNodes().size());

        eventPublisher.publishEvent(new MentorRoadmapChangeEvent(mentorRoadmap.getMentor().getJobId()));

        return new MentorRoadmapSaveResponse(
                mentorRoadmap.getId(),
                mentorRoadmap.getMentor().getId(),
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
        if (!mentorRoadmap.getMentor().getId().equals(mentorId)) {
            throw new ServiceException("403", "본인의 로드맵만 삭제할 수 있습니다.");
        }

        Long jobId = mentorRoadmap.getMentor().getJobId();

        // 1. 관련 노드들을 먼저 직접 삭제
        roadmapNodeRepository.deleteByRoadmapIdAndRoadmapType(
            roadmapId,
            RoadmapNode.RoadmapType.MENTOR
        );

        // 2. 로드맵 삭제
        mentorRoadmapRepository.delete(mentorRoadmap);

        log.info("멘토 로드맵 삭제 완료 - 멘토 ID: {}, 로드맵 ID: {}", mentorId, roadmapId);

        eventPublisher.publishEvent(new MentorRoadmapChangeEvent(jobId));
    }

    // taskId가 null인 자유입력 Task를 자동으로 pending alias로 등록
    private void registerPendingAliasesForFreeInputTasks(List<RoadmapNodeRequest> nodes) {
        for (RoadmapNodeRequest node : nodes) {
            if (node.taskId() == null && node.taskName() != null) {
                // TaskService를 통해 pending alias 자동 등록 (이미 존재하면 무시)
                taskService.createPendingAliasIfNotExists(node.taskName());
            }
        }
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
        int nodeCount = nodes.size();
        boolean[] stepExists = new boolean[nodeCount + 1]; // 1부터 nodeCount까지 사용

        // 중복 검증 및 stepOrder 수집
        for (RoadmapNodeRequest node : nodes) {
            int stepOrder = node.stepOrder();

            // 범위 검증
            if (stepOrder < 1 || stepOrder > nodeCount) {
                throw new ServiceException("400",
                    String.format("stepOrder는 1부터 %d 사이의 값이어야 합니다.", nodeCount));
            }

            // 중복 검증
            if (stepExists[stepOrder]) {
                throw new ServiceException("400", "stepOrder에 중복된 값이 있습니다");
            }

            stepExists[stepOrder] = true;
        }

        // 연속성 검증 (1부터 nodeCount까지 모든 값이 존재하는지 확인)
        for (int i = 1; i <= nodeCount; i++) {
            if (!stepExists[i]) {
                throw new ServiceException("400",
                    String.format("stepOrder는 1부터 시작하는 연속된 숫자여야 합니다."));
            }
        }
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

        // TaskService에 위임하여 검증 및 조회
        return taskService.validateAndGetTasks(taskIds);
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

            // estimatedHours 자동 계산 (hoursPerDay * weeks * 7)
            Integer estimatedHours = null;
            if (nodeRequest.hoursPerDay() != null && nodeRequest.weeks() != null) {
                estimatedHours = nodeRequest.hoursPerDay() * nodeRequest.weeks() * 7;
            }

            RoadmapNode node = RoadmapNode.builder()
                    .taskName(taskName)
                    .learningAdvice(nodeRequest.learningAdvice())
                    .recommendedResources(nodeRequest.recommendedResources())
                    .learningGoals(nodeRequest.learningGoals())
                    .difficulty(nodeRequest.difficulty())
                    .importance(nodeRequest.importance())
                    .hoursPerDay(nodeRequest.hoursPerDay())
                    .weeks(nodeRequest.weeks())
                    .estimatedHours(estimatedHours)
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