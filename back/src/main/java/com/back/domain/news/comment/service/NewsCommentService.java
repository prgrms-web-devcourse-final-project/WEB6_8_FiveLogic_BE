package com.back.domain.news.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.NewsComment;
import com.back.domain.news.comment.repository.NewsCommentRepository;
import com.back.domain.news.news.entity.News;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NewsCommentService {
    private final NewsCommentRepository newsCommentRepository;

    public NewsComment createComment(Member member, News news, String content) {
        NewsComment newsComment = NewsComment.create(member, news, content);
        return newsCommentRepository.save(newsComment);
    }

    public List<NewsComment> getComments(News news) {
        return newsCommentRepository.findByNews(news);
    }

    public NewsComment getCommentById(Long commentId) {
        return newsCommentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("404", "Comment not found: " + commentId));
    }

    public NewsComment updateComment(Member member, News news, Long commentId, String content) {
        NewsComment newsComment = getCommentById(commentId);

        if (!newsComment.getMember().equals(member)) {
            throw new ServiceException("403","You do not have permission to update this comment.");
        }
        if (!newsComment.getNews().equals(news)) {
            throw new ServiceException("400","This comment does not belong to the given news.");
        }
        newsComment.update(content);
        return newsComment;
    }

    public void deleteComment(Member member, News news, Long commentId) {
        NewsComment newsComment = getCommentById(commentId);

        if (!newsComment.getMember().equals(member)) {
            throw new ServiceException("403","You do not have permission to delete this comment.");
        }
        if (!newsComment.getNews().equals(news)) {
            throw new ServiceException("400","This comment does not belong to the given news.");
        }
        newsCommentRepository.delete(newsComment);
    }
}
