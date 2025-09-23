package com.back.domain.roadmap.task.service;

import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.entity.TaskAlias;
import com.back.domain.roadmap.task.repository.TaskAliasRepository;
import com.back.domain.roadmap.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public TaskAlias createAlias(Task task, String aliasName) {
        TaskAlias alias = new TaskAlias(aliasName);
        alias.setTask(task);
        return taskAliasRepository.save(alias);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    // 사용자가 입력한 키워드로 표준 Task 찾기 또는 새로운 TaskAlias 생성
    public List<Task> searchByKeyword(String keyword){

        // 1. Task(표준 이름) 직접 검색
        List<Task> directMatches = taskRepository.findByNameContainingIgnoreCase(keyword);
        Set<Long> directMatchIds = directMatches.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        // 2. TaskAlias(별칭) 검색
        List<Task> aliasMatches = taskAliasRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .filter(alias -> alias.getTask() != null)
                .map(TaskAlias::getTask)
                .filter(task -> !directMatchIds.contains(task.getId()))
                .toList();

        List<Task> results = new ArrayList<>(directMatches);
        results.addAll(aliasMatches);
        return results;
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
