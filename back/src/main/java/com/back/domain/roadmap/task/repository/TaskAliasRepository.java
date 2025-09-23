package com.back.domain.roadmap.task.repository;

import com.back.domain.roadmap.task.entity.TaskAlias;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskAliasRepository extends JpaRepository<TaskAlias, Long> {
    Optional<TaskAlias> findByNameIgnoreCase(String name);

    @Query("SELECT ta FROM TaskAlias ta JOIN FETCH ta.task t WHERE LOWER(ta.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND ta.task IS NOT NULL")
    List<TaskAlias> findByNameContainingIgnoreCaseWithTask(@Param("keyword") String keyword);

    Page<TaskAlias> findByTaskIsNull(Pageable pageable); // pending alias 목록 페이징 조회
}
