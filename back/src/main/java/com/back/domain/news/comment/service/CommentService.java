package com.back.domain.news.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.comment.repository.CommentRepository;
import com.back.domain.news.news.entity.News;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
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
