package com.back.domain.roadmap.task.repository;

import com.back.domain.roadmap.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByName(String name);
    Optional<Task> findByNameIgnoreCase(String name);
    List<Task> findByNameContainingIgnoreCase(String keyword);
}
