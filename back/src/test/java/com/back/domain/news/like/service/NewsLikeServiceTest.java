package com.back.domain.news.like.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.entity.NewsLike;
import com.back.domain.news.like.repository.NewsLikeRepository;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsLikeServiceTest {

    @Mock
    private NewsLikeRepository newsLikeRepository;

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsLikeService newsLikeService;

    @Test
    @DisplayName("사용자는 뉴스에 '좋아요'를 누를 수 있다.")
    void likeNews_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long newsId = news.getId();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(newsLikeRepository.findByMemberAndNews(member, news)).thenReturn(Optional.empty());

        // when
        newsLikeService.likeNews(member, newsId);

        // then
        verify(newsLikeRepository, times(1)).save(any(NewsLike.class));
    }

    @Test
    @DisplayName("존재하지 않는 뉴스에 '좋아요'를 누르면 예외가 발생한다.")
    void likeNews_NewsNotFound_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        Long nonExistentNewsId = 999L;

        when(newsRepository.findById(nonExistentNewsId)).thenReturn(Optional.empty());

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsLikeService.likeNews(member, nonExistentNewsId);
        });

        assertThat(exception.getMessage()).isEqualTo("404 : 해당 뉴스를 찾을 수 없습니다.");
        verify(newsLikeRepository, never()).save(any(NewsLike.class));
    }

    @Test
    @DisplayName("이미 '좋아요'를 누른 뉴스에 다시 '좋아요'를 누르면 예외가 발생한다.")
    void likeNews_AlreadyLiked_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long newsId = news.getId();
        NewsLike existingNewsLike = NewsLike.create(member, news);

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(newsLikeRepository.findByMemberAndNews(member, news)).thenReturn(Optional.of(existingNewsLike));

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsLikeService.likeNews(member, newsId);
        });

        assertThat(exception.getMessage()).isEqualTo("400 : 이미 좋아요를 누른 뉴스입니다.");
        verify(newsLikeRepository, never()).save(any(NewsLike.class));
    }
}
