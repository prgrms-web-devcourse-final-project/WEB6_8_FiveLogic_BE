package com.back.domain.roadmap.task.repository;

import com.back.domain.roadmap.task.entity.TaskAlias;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAliasRepository extends JpaRepository<TaskAlias, Long> {
    Optional<TaskAlias> findByNameIgnoreCase(String name);
    Page<TaskAlias> findByTaskIsNull(Pageable pageable); // pending alias 목록 페이징 조회
}
