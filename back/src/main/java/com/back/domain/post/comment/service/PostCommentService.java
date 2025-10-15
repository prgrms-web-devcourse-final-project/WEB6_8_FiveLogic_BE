package com.back.domain.post.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.dto.CommentDeleteRequest;
import com.back.domain.post.comment.dto.CommentModifyRequest;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.repository.PostCommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.exception.ServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostService postService;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    public void createComment(Member member, Long postId, CommentCreateRequest commentCreateRequest) {
        validateComment(commentCreateRequest.comment());
        Post post = postService.findPostById(postId);

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
        PostComment postComment = findCommentById(commentDeleteRequest.commentId());
        Member author = postComment.getMember();

        validateAuthorized(member,author);
        validatePostExists(postId);

        postCommentRepository.delete(postComment);
    }

    @Transactional
    public void updatePostComment(Long postId, CommentModifyRequest commentModifyRequest, Member member) {
        validatePostExists(postId);

        PostComment postComment = findCommentById(commentModifyRequest.commentId());
        Member author = postComment.getMember();

        validateAuthorized(member, author);
        validateComment(commentModifyRequest.content());

        postComment.updateContent(commentModifyRequest.content());
    }

    @Transactional
    public void adoptComment(Long commentId, Member member) {
        PostComment postComment = findCommentById(commentId);

        Post post = postComment.getPost();

        validateIsPostAuthor(post, member);
        //validatePostType(post);
        validateAlreadyAdoptedComment(postComment);
        validateAlreadyExistsAdoptedComment(post);

        postComment.adoptComment();
        post.updateResolveStatus(true);
    }

    @Transactional
    public  CommentAllResponse getAdoptedComment(Long postId) {
        Post post = postService.findPostById(postId);

        validatePostType(post);

        PostComment postComment = validateAdoptedComment(post);
        return CommentAllResponse.from(postComment);
    }





    // ========== 헬퍼 메소드들 ========== //
    private void validateIsPostAuthor(Post post, Member member){
        if (!post.isAuthor(member)) {
            throw new ServiceException("400", "채택 권한이 없습니다.");
        }
    }

    private void validateAlreadyAdoptedComment(PostComment postComment){
        if (postComment.getIsAdopted()) {
            throw new ServiceException("400", "이미 채택된 댓글입니다.");
        }
    }

    private void validateAlreadyExistsAdoptedComment(Post post) {
        if (postCommentRepository.existsByPostAndIsAdoptedTrue(post)) {
            throw new ServiceException("400", "이미 채택된 댓글이 있습니다.");
        }
    }

    private void validateAuthorized(Member member, Member author) {
        if(!Objects.equals(member.getId(), author.getId())) {
            throw new ServiceException("400", "변경 권한이 없습니다.");
        }
    }

    private PostComment validateAdoptedComment(Post post) {
        return postCommentRepository.findByPostAndIsAdoptedTrue(post)
                .orElseThrow(() -> new ServiceException("400", "채택된 댓글이 없습니다."));
    }

    private void validatePostType(Post post) {
        if (post.getPostType() != Post.PostType.QUESTIONPOST) {
            throw new ServiceException("400", "질문 게시글만 채택된 댓글을 가질 수 있습니다.");
        }
    }

    private void validateComment(String comment) {
        if (comment == null || comment.isEmpty()) {
            throw new ServiceException("400", "댓글은 비어 있을 수 없습니다.");
        }
    }

    private void validatePostExists(Long postId) {
        if (!postService.existsById(postId)) {
            throw new ServiceException("400", "유효하지 않은 게시글 Id입니다.");
        }
    }

    private PostComment findCommentById(Long commentId) {
        return postCommentRepository.findById(commentId).orElseThrow(() -> new ServiceException("400", "해당 Id의 댓글이 없습니다."));
    }
}
