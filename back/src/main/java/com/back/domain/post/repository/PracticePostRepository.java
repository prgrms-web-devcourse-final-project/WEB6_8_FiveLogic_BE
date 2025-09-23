package com.back.domain.post.repository;

import com.back.domain.post.entity.PracticePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticePostRepository extends JpaRepository<PracticePost, Long> {

}
