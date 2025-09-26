package com.back.domain.roadmap.task.dto;

import com.back.domain.roadmap.task.entity.Task;

public record TaskDto(
        Long id,
        String name
) {
    public TaskDto(Task task){
        this(task.getId(),task.getName());
    }
}
