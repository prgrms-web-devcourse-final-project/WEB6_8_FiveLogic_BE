package com.back.domain.post.comment.repository;

import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("SELECT c FROM PostComment c JOIN FETCH c.member WHERE c.post.id = :postId")
    List<PostComment> findCommentsWithMemberByPostId(@Param("postId") Long postId);


    boolean existsByPostAndIsAdoptedTrue(Post post);

    Optional<PostComment> findByPostAndIsAdoptedTrue(Post post);
}
