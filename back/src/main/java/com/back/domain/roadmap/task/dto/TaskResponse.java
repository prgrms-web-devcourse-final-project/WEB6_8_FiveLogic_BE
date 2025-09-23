package com.back.domain.roadmap.task.dto;

import com.back.domain.roadmap.task.entity.Task;

public record TaskResponse(
        Long id,
        String name
) {
    public TaskResponse(Task task){
        this(task.getId(),task.getName());
    }
}
