package com.back.domain.roadmap.task.dto;

import com.back.domain.roadmap.task.entity.TaskAlias;

public record TaskAliasDetailDto(
        Long aliasId,
        String aliasName,
        Long taskId,
        String taskName
) {
    public TaskAliasDetailDto(TaskAlias alias){
        this(
                alias.getId(),
                alias.getName(),
                alias.getTask().getId(),
                alias.getTask().getName()
        );
    }
}
