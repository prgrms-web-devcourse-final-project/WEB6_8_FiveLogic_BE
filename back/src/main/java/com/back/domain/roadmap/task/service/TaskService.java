package com.back.domain.roadmap.task.service;

import com.back.domain.roadmap.task.dto.TaskAliasDto;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.entity.TaskAlias;
import com.back.domain.roadmap.task.repository.TaskAliasRepository;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskAliasRepository taskAliasRepository;

    @Transactional(readOnly = true)
    public long count() {
        return taskRepository.count();
    }

    @Transactional
    public Task create(String name) {
        Task task = new Task(name);
        return taskRepository.save(task);
    }

    @Transactional
    public TaskAlias createAlias(Task task, String aliasName) {
        TaskAlias alias = new TaskAlias(aliasName);
        alias.linkToTask(task);
        return taskAliasRepository.save(alias);
    }

    // 사용자가 입력한 키워드 기반 검색
    @Transactional(readOnly = true)
    public List<Task> searchByKeyword(String keyword){
        return taskRepository.findTasksByKeyword(keyword);
    }

    /**
     * 사용자가 명시적으로 새 Task를 제안할 때 사용
     * 이미 존재하는 경우 예외를 던져 사용자에게 알림
     */
    @Transactional
    public TaskAlias createPendingAlias(String taskName){
        // Task나 TaskAlias에 이미 존재하는 이름인지 검증
        validateNewPendingAliasName(taskName);

        // 모든 검증 통과 시 새로운 pending alias 생성
        TaskAlias pendingAlias = new TaskAlias(taskName);
        return taskAliasRepository.save(pendingAlias);
    }

    /**
     * 로드맵 생성 시 자동으로 pending alias를 등록할 때 사용
     * 이미 존재하는 경우 무시 (다른 사용자가 이미 제안했을 수 있음)
     */
    @Transactional
    public void createPendingAliasIfNotExists(String taskName) {
        // TaskAlias에 이미 존재하는지 확인
        Optional<TaskAlias> existingAlias = taskAliasRepository.findByNameIgnoreCase(taskName);
        if (existingAlias.isPresent()) {
            return; // 이미 존재하면 무시
        }

        // Task에 이미 존재하는지 확인
        Optional<Task> existingTask = taskRepository.findByNameIgnoreCase(taskName);
        if (existingTask.isPresent()) {
            return; // 이미 존재하면 무시
        }

        // 존재하지 않으면 새로운 pending alias 생성
        TaskAlias pendingAlias = new TaskAlias(taskName);
        taskAliasRepository.save(pendingAlias);
    }

    /**
     * Task ID 목록을 검증하고 Map으로 반환 (로드맵 생성 시 사용)
     * 존재하지 않는 Task ID가 있으면 예외 발생
     */
    @Transactional(readOnly = true)
    public Map<Long, Task> validateAndGetTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of(); // 빈 맵 반환
        }

        // 일괄 조회로 존재하는 Task들 확인
        List<Task> existingTasks = taskRepository.findAllById(taskIds);
        Map<Long, Task> existingTaskMap = existingTasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

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

    // === 관리자용 기능들 ===
    // Pending alias 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<TaskAliasDto> getPendingTaskAliases(Pageable pageable) {
        Page<TaskAlias> pendingTaskAliases = taskAliasRepository.findByTaskIsNull(pageable);
        return pendingTaskAliases.map(taskAlias ->
                new TaskAliasDto(taskAlias.getId(), taskAlias.getName()));
    }

    // Pending alias를 기존 Task와 연결
    @Transactional
    public TaskAlias linkPendingAlias(Long aliasId, Long taskId) {
        TaskAlias pendingAlias = findPendingAliasById(aliasId);
        Task task = findTaskById(taskId);

        pendingAlias.linkToTask(task);
        return taskAliasRepository.save(pendingAlias);
    }

    // Pending alias를 새로운 Task로 생성
    @Transactional
    public Task createTaskFromPending(Long aliasId) {
        TaskAlias pendingAlias = findPendingAliasById(aliasId);

        // 동일한 이름의 Task가 이미 존재하는지 확인
        validateTaskNameForCreation(pendingAlias.getName());

        // 새 Task 생성
        Task newTask = create(pendingAlias.getName());

        // pending alias를 새 Task와 연결
        pendingAlias.linkToTask(newTask);
        taskAliasRepository.save(pendingAlias);

        return newTask;
    }

    // Pending alias 삭제
    @Transactional
    public void deletePendingAlias(Long aliasId) {
        TaskAlias pendingAlias = findPendingAliasById(aliasId);
        taskAliasRepository.delete(pendingAlias);
    }

    // === 검증 로직 메서드들 ===

    // TaskAlias를 ID로 조회하고 Pending 상태인지 검증
    private TaskAlias findPendingAliasById(Long aliasId) {
        TaskAlias alias = taskAliasRepository.findById(aliasId)
                .orElseThrow(() -> new ServiceException("404", "해당 별칭이 존재하지 않습니다."));

        if (!alias.isPending()) {
            throw new ServiceException("400", "이미 연결된 별칭입니다.");
        }

        return alias;
    }

    // Task를 ID로 조회
    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ServiceException("404", "해당 Task가 존재하지 않습니다."));
    }

    // Task와 TaskAlias 모두 중복 검증 (새로운 pending alias 생성 시 사용)
    private void validateNewPendingAliasName(String taskName) {
        // 1. TaskAlias 테이블에서 중복 확인 (Pending 포함)
        Optional<TaskAlias> existingAliasOpt = taskAliasRepository.findByNameIgnoreCase(taskName);
        if (existingAliasOpt.isPresent()) {
            TaskAlias existingAlias = existingAliasOpt.get();
            if (!existingAlias.isPending()) {
                throw new ServiceException("400", "이미 등록된 Task의 별칭입니다.");
            } else {
                throw new ServiceException("400", "이미 제안된 Task명입니다.");
            }
        }

        // 2. Task 테이블에서 중복 검증
        validateTaskNameForCreation(taskName);
    }

    // 동일한 이름의 Task가 이미 존재하는지 확인 (pending alias를 새 Task로 등록할 때 사용)
    private void validateTaskNameForCreation(String taskName) {
        Optional<Task> existingTaskOpt = taskRepository.findByNameIgnoreCase(taskName);
        if (existingTaskOpt.isPresent()) {
            throw new ServiceException("400", "이미 등록된 Task명입니다.");
        }
    }
}
