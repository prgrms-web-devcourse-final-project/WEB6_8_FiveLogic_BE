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

    // 사용자가 입력한 키워드로 표준 Task 찾기 또는 새로운 TaskAlias 생성
    public List<Task> searchByKeyword(String keyword){

        // 1. 표준 Task 이름 직접 검색
        List<Task> directMatches = taskRepository.findByNameContainingIgnoreCase(keyword);
        List<Task> results = new ArrayList<>(directMatches);

        // 2. TaskAlias 검색
        List<TaskAlias> aliasMatches = taskAliasRepository.findByNameContainingIgnoreCase(keyword);
        aliasMatches.stream()
                .filter(alias -> alias.getTask() != null) // pending 상태의 alias 제외
                .filter(alias -> directMatches.stream()
                        .noneMatch(task -> task.getId().equals(alias.getTask().getId()))) // Task 검색 결과와 중복되는 값 제거
                .forEach(alias ->
                        results.add(alias.getTask())
                );

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
