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

    public Comment updateComment(Member member, Long commentId, String content) {
        Comment comment = getCommentById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getMember().equals(member)) {
            throw new IllegalArgumentException("You do not have permission to update this comment.");
        }
        return comment.update(content);
    }

    public void deleteComment(Member member, Long commentId) {
        Comment comment = getCommentById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getMember().equals(member)) {
            throw new IllegalArgumentException("You do not have permission to delete this comment.");
        }
        commentRepository.delete(comment);
    }
}
