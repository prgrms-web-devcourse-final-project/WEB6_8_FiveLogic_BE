package com.back.domain.roadmap.task.dto;

import com.back.domain.roadmap.task.entity.Task;

public record TaskSearchResponse(
        Long id,
        String name
) {
    public TaskSearchResponse(Task task){
        this(task.getId(),task.getName());
    }
}
