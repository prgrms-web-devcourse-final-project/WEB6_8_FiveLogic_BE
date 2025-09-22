package com.back.domain.roadmap.task.controller;

import com.back.domain.roadmap.task.dto.TaskSearchResponse;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.service.TaskService;
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
    public List<TaskSearchResponse> searchTasks(@RequestParam String keyword) {
        if(keyword == null || keyword.trim().isEmpty()){
            return Collections.emptyList();
        }

        List<Task> tasks = taskService.searchByKeyword(keyword.trim());
        return tasks.stream()
                .map(TaskSearchResponse::new)
                .toList();
    }
}
