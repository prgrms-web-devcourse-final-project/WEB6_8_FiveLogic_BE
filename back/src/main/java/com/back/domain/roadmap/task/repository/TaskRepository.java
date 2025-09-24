package com.back.domain.roadmap.task.repository;

import com.back.domain.roadmap.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByNameIgnoreCase(String name);

    @Query("""
        SELECT DISTINCT t FROM Task t 
        LEFT JOIN FETCH t.aliases ta
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR (ta.task IS NOT NULL AND LOWER(ta.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY t.name
        """)
    List<Task> findTasksByKeyword(@Param("keyword") String keyword);
}
