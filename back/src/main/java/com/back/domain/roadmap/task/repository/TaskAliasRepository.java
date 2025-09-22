package com.back.domain.roadmap.task.repository;

import com.back.domain.roadmap.task.entity.TaskAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskAliasRepository extends JpaRepository<TaskAlias, Long> {
    Optional<TaskAlias> findByName(String name);
    List<TaskAlias> findByTaskIsNull(); // pending 상태인 alias 조회
}
