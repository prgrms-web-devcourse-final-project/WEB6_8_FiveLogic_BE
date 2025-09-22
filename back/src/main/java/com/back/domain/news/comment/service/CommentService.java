package com.back.domain.news.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.comment.repository.CommentRepository;
import com.back.domain.news.news.entity.News;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional
    public Comment createComment(Member member, News news, String content) {
        Comment comment = Comment.create(member, news, content);
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(News news) {
        return commentRepository.findByNews(news);
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public Comment updateComment(Member member, News news, Long commentId, String content) {
        Comment comment = getCommentById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getMember().equals(member)) {
            throw new IllegalArgumentException("You do not have permission to update this comment.");
        }
        if (!comment.getNews().equals(news)) {
            throw new IllegalArgumentException("This comment does not belong to the given news.");
        }
        comment.update(content);
        return comment;
    }

    public void deleteComment(Member member, News news, Long commentId) {
        Comment comment = getCommentById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getMember().equals(member)) {
            throw new IllegalArgumentException("You do not have permission to delete this comment.");
        }
        if (!comment.getNews().equals(news)) {
            throw new IllegalArgumentException("This comment does not belong to the given news.");
        }
        commentRepository.delete(comment);
    }
}
