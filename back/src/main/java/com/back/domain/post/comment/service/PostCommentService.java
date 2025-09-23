package com.back.domain.post.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.repository.PostCommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    public void createComment(Member member, Long postId, CommentCreateRequest commentCreateRequest) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        PostComment postComment = new PostComment();

        postComment.setContent(commentCreateRequest.getComment());
        postComment.setMember(member);
        postComment.setRole(String.valueOf(member.getRole()));

        post.addComment(postComment);

        postCommentRepository.save(postComment);

    }
}
