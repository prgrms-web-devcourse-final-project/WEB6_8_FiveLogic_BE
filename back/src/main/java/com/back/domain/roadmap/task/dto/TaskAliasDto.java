package com.back.domain.roadmap.task.dto;

import com.back.domain.roadmap.task.entity.TaskAlias;

public record TaskAliasDto(
        Long aliasId,
        String aliasName
) {
    public TaskAliasDto(TaskAlias alias){
        this(
                alias.getId(),
                alias.getName()
        );
    }
}
