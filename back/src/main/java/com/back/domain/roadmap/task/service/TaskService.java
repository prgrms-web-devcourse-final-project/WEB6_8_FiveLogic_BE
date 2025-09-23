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
        alias.setTask(task);
        return taskAliasRepository.save(alias);
    }

    // 사용자가 입력한 키워드 기반 검색
    @Transactional(readOnly = true)
    public List<Task> searchByKeyword(String keyword){
        // 1. Task(표준 이름) 직접 검색
        List<Task> directMatches = taskRepository.findByNameContainingIgnoreCase(keyword);
        Set<Long> directMatchIds = directMatches.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        // 2. TaskAlias(별칭) 검색
        List<TaskAlias> aliasMatchesWithTask = taskAliasRepository.findByNameContainingIgnoreCaseWithTask(keyword);
        List<Task> aliasMatches = aliasMatchesWithTask.stream()
                .map(TaskAlias::getTask)
                .filter(Objects::nonNull)
                .filter(task -> !directMatchIds.contains(task.getId()))
                .toList();

        List<Task> results = new ArrayList<>(directMatches);
        results.addAll(aliasMatches);
        return results;
    }

    @Transactional
    public TaskAlias createPendingAlias(String taskName){
        // 1. 먼저 Alias가 존재하는지 확인 (Pending 포함)
        Optional<TaskAlias> existingAliasOpt = taskAliasRepository.findByNameIgnoreCase(taskName);
        if (existingAliasOpt.isPresent()) {
            TaskAlias existingAlias = existingAliasOpt.get();
            if (existingAlias.getTask() != null) {
                throw new ServiceException("400", "이미 등록된 task의 별칭입니다.");
            } else {
                throw new ServiceException("400", "이미 제안된 task입니다.");
            }
        }

        // 2. Alias가 없다면, Task 테이블에 직접 존재하는지 확인
        Optional<Task> existingTaskOpt = taskRepository.findByNameIgnoreCase(taskName);
        if(existingTaskOpt.isPresent()){
            throw new ServiceException("400", "이미 등록된 task입니다.");
        }

        // 3. 모두 해당 없으면 새로운 pending alias 생성
        TaskAlias pendingAlias = new TaskAlias(taskName);
        return taskAliasRepository.save(pendingAlias);
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
        TaskAlias pendingAlias = taskAliasRepository.findById(aliasId)
                .orElseThrow(() -> new ServiceException("404", "해당 별칭이 존재하지 않습니다."));

        if (pendingAlias.getTask() != null) {
            throw new ServiceException("400", "이미 연결된 별칭입니다.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ServiceException("404", "해당 Task가 존재하지 않습니다."));

        pendingAlias.setTask(task);
        return taskAliasRepository.save(pendingAlias);
    }

    // Pending alias를 새로운 Task로 생성
    @Transactional
    public Task createTaskFromPending(Long aliasId) {
        TaskAlias pendingAlias = taskAliasRepository.findById(aliasId)
                .orElseThrow(() -> new ServiceException("404", "해당 별칭이 존재하지 않습니다."));

        if (pendingAlias.getTask() != null) {
            throw new ServiceException("400", "이미 연결된 별칭입니다.");
        }

        // 동일한 이름의 Task가 이미 존재하는지 확인
        Optional<Task> existingTask = taskRepository.findByNameIgnoreCase(pendingAlias.getName());
        if (existingTask.isPresent()) {
            throw new ServiceException("400", "이미 존재하는 Task 이름입니다.");
        }

        // 새 Task 생성
        Task newTask = create(pendingAlias.getName());

        // pending alias를 새 Task와 연결
        pendingAlias.setTask(newTask);
        taskAliasRepository.save(pendingAlias);

        return newTask;
    }

    // Pending alias 삭제
    @Transactional
    public void deletePendingAlias(Long aliasId) {
        TaskAlias pendingAlias = taskAliasRepository.findById(aliasId)
                .orElseThrow(() -> new ServiceException("404", "해당 별칭이 존재하지 않습니다."));

        if (pendingAlias.getTask() != null) {
            throw new ServiceException("400", "연결된 별칭은 삭제할 수 없습니다.");
        }

        taskAliasRepository.delete(pendingAlias);
    }
}
