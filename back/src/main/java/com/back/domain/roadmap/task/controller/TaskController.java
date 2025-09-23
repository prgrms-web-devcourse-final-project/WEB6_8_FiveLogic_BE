package com.back.domain.roadmap.task.controller;

import com.back.domain.roadmap.task.dto.TaskResponse;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.service.TaskService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/search")
    public RsData<List<TaskResponse>> searchTasks(@RequestParam String keyword) {
        // 입력값 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            return new RsData<>(
                    "200",
                    "검색 결과가 없습니다.",
                    Collections.emptyList()
            );
        }

        List<Task> tasks = taskService.searchByKeyword(keyword.trim());
        List<TaskResponse> responses =  tasks.stream()
                .map(TaskResponse::new)
                .toList();

        return new RsData<>(
                "200",
                "Task 검색 성공",
                responses
        );
    }
}
