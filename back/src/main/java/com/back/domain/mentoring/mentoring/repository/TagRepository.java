package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    List<Tag> findByNameIn(List<String> names);
}
