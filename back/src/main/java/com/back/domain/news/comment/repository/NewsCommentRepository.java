package com.back.domain.news.comment.repository;

import com.back.domain.news.comment.entity.NewsComment;
import com.back.domain.news.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {
    List<NewsComment> findByNews(News news);
}
