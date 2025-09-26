package com.back.domain.post.like.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.like.entity.PostLike;
import com.back.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByMemberAndPost(Member member, Post post);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.status = 'LIKE'")
    int countLikesByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.status = 'DISLIKE'")
    int countDislikesByPostId(@Param("postId") Long postId);

    boolean existsByMemberAndPost(Member member, Post post);
}