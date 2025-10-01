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
                .content(commentCreateRequest.comment())
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

        PostComment postComment = getPostCommentById(commentDeleteRequest.commentId());
        Member author = postComment.getMember();


        if(!Objects.equals(member.getId(), author.getId())) {
            throw new ServiceException("400", "삭제 권한이 없습니다.");
        }

//        if(postComment.getIsAdopted()) {
//            throw new ServiceException("400", "채택된 댓글은 삭제할 수 없습니다.");
//        }

        postCommentRepository.delete(postComment);

    }

    @Transactional
    public void updatePostComment(Long postId, CommentModifyRequest commentModifyRequest, Member member) {
        validatePostExists(postId);

        PostComment postComment = getPostCommentById(commentModifyRequest.commentId());
        Member author = postComment.getMember();


        if(!Objects.equals(member.getId(), author.getId())) {
            throw new ServiceException("400", "수정 권한이 없습니다.");
        }

        if ( commentModifyRequest.content() == null || commentModifyRequest.content().isEmpty()) {
            throw new ServiceException("400", "댓글은 비어 있을 수 없습니다.");
        }

        postComment.updateContent(commentModifyRequest.content());
    }



    private void validatePostExists(Long postId) {
        if(postId == null || postId <= 0) {
            throw new ServiceException("400", "유효하지 않은 게시글 Id입니다.");
        }

        if (!postRepository.existsById(postId)) {
            throw new ServiceException("400", "해당 Id의 게시글이 없습니다.");
        }


    }

    private PostComment getPostCommentById(Long commentId) {
        return postCommentRepository.findById(commentId).orElseThrow(() -> new ServiceException("400", "해당 Id의 댓글이 없습니다."));
    }

    @Transactional
    public void adoptComment(Long commentId, Member member) {
        PostComment postComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("400", "해당 Id의 댓글이 없습니다."));

        Post post = postComment.getPost();

        if (!post.isAuthor(member)) {
            throw new ServiceException("400", "채택 권한이 없습니다.");
        }

        if (post.getPostType() != Post.PostType.QUESTIONPOST) {
            throw new ServiceException("400", "질문 게시글에만 댓글 채택이 가능합니다.");
        }

        if (postComment.getIsAdopted()) {
            throw new ServiceException("400", "이미 채택된 댓글입니다.");
        }

        // 이미 채택된 댓글이 있는지 확인
        boolean alreadyAdopted = postCommentRepository.existsByPostAndIsAdoptedTrue(post);
        if (alreadyAdopted) {
            throw new ServiceException("400", "이미 채택된 댓글이 있습니다.");
        }

        postComment.adoptComment();

        post.updateResolveStatus(true);
    }
}
