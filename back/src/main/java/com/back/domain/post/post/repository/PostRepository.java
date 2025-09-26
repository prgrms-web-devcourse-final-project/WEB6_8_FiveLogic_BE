package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> , PostRepositoryCustom{
    @Query("SELECT p FROM Post p JOIN FETCH p.member")
    List<Post> findAllWithMember();

    @Query("SELECT p FROM Post p JOIN FETCH p.member WHERE p.id = :postId")
    Optional<Post> findByIdWithMember(@Param("postId") Long postId);

}
