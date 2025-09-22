package com.back.domain.post.repository;

import com.back.domain.post.entity.QuestionPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPostRepository extends JpaRepository<QuestionPost, Long> {

}
