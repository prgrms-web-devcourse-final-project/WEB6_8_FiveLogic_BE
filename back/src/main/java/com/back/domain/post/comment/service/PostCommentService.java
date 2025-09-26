package com.back.domain.post.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.dto.CommentDeleteRequest;
import com.back.domain.post.comment.dto.CommentModifyRequest;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.repository.PostCommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    public void createComment(Member member, Long postId, CommentCreateRequest commentCreateRequest) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        PostComment postComment = PostComment.builder()
                .post(post)
                .content(commentCreateRequest.getComment())
                .member(member)
                .role(member.getRole().name())
                .build();

        post.addComment(postComment);

        postCommentRepository.save(postComment);

    }

    @Transactional
    public List<CommentAllResponse> getAllPostCommentResponse(Long postId) {
        validatePostExists(postId);

        List<PostComment> listPostComment = postCommentRepository.findCommentsWithMemberByPostId(postId);

        return listPostComment.stream()
                .map(CommentAllResponse::from)
                .toList();
    }

    @Transactional
    public void removePostComment(Long postId, CommentDeleteRequest commentDeleteRequest, Member member) {
        validatePostExists(postId);

        PostComment postComment = getPostCommentById(commentDeleteRequest.getCommentId());
        Member author = postComment.getMember();


        if(!Objects.equals(member.getId(), author.getId())) {
            throw new ServiceException("400", "삭제 권한이 없습니다.");
        }

        postCommentRepository.delete(postComment);

    }

    @Transactional
    public void updatePostComment(Long postId, CommentModifyRequest commentModifyRequest, Member member) {
        validatePostExists(postId);

        PostComment postComment = getPostCommentById(commentModifyRequest.getCommentId());
        Member author = postComment.getMember();


        if(!Objects.equals(member.getId(), author.getId())) {
            throw new ServiceException("400", "수정 권한이 없습니다.");
        }

        postComment.updateContent(commentModifyRequest.getContent());
    }



    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ServiceException("400", "해당 Id의 게시글이 없습니다.");
        }
    }

    private PostComment getPostCommentById(Long commentId) {
        return postCommentRepository.findById(commentId).orElseThrow(() -> new ServiceException("400", "해당 Id의 댓글이 없습니다."));
    }



}
