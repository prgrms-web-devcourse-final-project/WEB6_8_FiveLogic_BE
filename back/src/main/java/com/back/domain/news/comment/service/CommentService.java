package com.back.domain.news.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.comment.repository.CommentRepository;
import com.back.domain.news.news.entity.News;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 의도하지 않은 어노테이션입니다. 이후 삭제하고 재도입을 고려중입니다.
// 일반적인 CRUD가 있는 서비스입니다. 특이사항은 없습니다.
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment createComment(Member member, News news, String content) {
        Comment comment = Comment.create(member, news, content);
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(News news) {
        return commentRepository.findByNews(news);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found: " + commentId));
    }

    // 댓글 작성자만 수정할 수 있습니다.
    public Comment updateComment(Member member, News news, Long commentId, String content) {
        Comment comment = getCommentById(commentId);

        if (!comment.getMember().equals(member)) {
            throw new AccessDeniedException("You do not have permission to update this comment.");
        }
        if (!comment.getNews().equals(news)) {
            throw new IllegalArgumentException("This comment does not belong to the given news.");
        }
        comment.update(content);
        return comment;
    }

    // 댓글 작성자만 삭제할 수 있습니다.
    public void deleteComment(Member member, News news, Long commentId) {
        Comment comment = getCommentById(commentId);

        if (!comment.getMember().equals(member)) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }
        if (!comment.getNews().equals(news)) {
            throw new IllegalArgumentException("This comment does not belong to the given news.");
        }
        commentRepository.delete(comment);
    }
}
