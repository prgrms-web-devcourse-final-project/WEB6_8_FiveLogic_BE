package com.back.domain.news.like.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.entity.Like;
import com.back.domain.news.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByMemberAndNews(Member member, News news);
}
