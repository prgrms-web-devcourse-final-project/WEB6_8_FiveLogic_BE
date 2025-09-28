package com.back.domain.news.like.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.entity.NewsLike;
import com.back.domain.news.like.repository.NewsLikeRepository;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsLikeService {
    private final NewsLikeRepository newsLikeRepository;
    private final NewsRepository newsRepository;

    public NewsLike likeNews(Member member, Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ServiceException("404", "해당 뉴스를 찾을 수 없습니다."));

        newsLikeRepository.findByMemberAndNews(member, news)
                .ifPresent(like -> {
                    throw new ServiceException("400", "이미 좋아요를 누른 뉴스입니다.");
                });

        NewsLike newsLike = NewsLike.create(member, news);
        return newsLikeRepository.save(newsLike);
    }

    public long getLikeCount(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ServiceException("404", "해당 뉴스를 찾을 수 없습니다."));
        return newsLikeRepository.countByNews(news);
    }
}