package com.back.domain.roadmap.task.dto;

import com.back.domain.roadmap.task.entity.TaskAlias;

import java.time.LocalDateTime;

public record CreatePendingAliasResponse(
        Long aliasId,
        String aliasName,
        LocalDateTime createTime
) {
    public CreatePendingAliasResponse(TaskAlias alias){
        this(
                alias.getId(),
                alias.getName(),
                alias.getCreateDate()
        );
    }
}
