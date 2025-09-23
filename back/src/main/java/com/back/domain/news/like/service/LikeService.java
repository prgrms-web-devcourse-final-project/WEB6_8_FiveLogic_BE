package com.back.domain.news.like.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.entity.Like;
import com.back.domain.news.like.repository.LikeRepository;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final NewsRepository newsRepository;

    /**
     * 뉴스에 좋아요를 추가합니다.
     * 중복 좋아요는 허용하지 않습니다.
     * 현재는 좋아요 취소기능은 없습니다.
     */
    public Like likeNews(Member member, Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스를 찾을 수 없습니다."));  //NoSuchElementException 으로 처리예정입니다.

        Optional<Like> existingLike = likeRepository.findByMemberAndNews(member, news);
        if (existingLike.isPresent()) {
            throw new IllegalStateException("이미 좋아요를 누른 뉴스입니다.");
        }

        Like like = Like.create(member, news);
        return likeRepository.save(like);
    }

    /**
     * 뉴스의 좋아요 수를 조회합니다.
     */
    public long getLikeCount(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스를 찾을 수 없습니다."));
        return likeRepository.countByNews(news);
    }
}