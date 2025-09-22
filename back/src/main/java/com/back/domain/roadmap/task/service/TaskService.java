package com.back.domain.roadmap.task.service;

import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.entity.TaskAlias;
import com.back.domain.roadmap.task.repository.TaskAliasRepository;
import com.back.domain.roadmap.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskAliasRepository taskAliasRepository;

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

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    // 사용자 입력값으로 표준 Task 찾기 또는 새로운 TaskAlias 생성
    public Task findOrCreateTask(String input){
        // 1. 정확한 이름 매칭 (대소문자 구분 없음)
        Optional<Task> exactMatch = taskRepository.findByNameIgnoreCase(input);
        if(exactMatch.isPresent()){
            return exactMatch.get();
        }

        // 2. Alias 매칭 (대소문자 구분 없음)
        Optional<TaskAlias> aliasMatch = taskAliasRepository.findByNameIgnoreCase(input);
        if(aliasMatch.isPresent() && aliasMatch.get().getTask() != null) {
            return aliasMatch.get().getTask();
        }

        // 3. 매칭 실패 시 pending 상태로 Alias 생성
        createPendingAlias(input);
        return null; // 표준 Task가 없으므로 null 반환
    }

    @Transactional
    public void createPendingAlias(String input){
        // 이미 존재하는 pending alias인지 확인
        Optional<TaskAlias> existingPending = taskAliasRepository.findByNameIgnoreCase(input);
        if(existingPending.isEmpty()){
            TaskAlias pendingAlias = new TaskAlias(input);
            taskAliasRepository.save(pendingAlias);
        }
    }
}
