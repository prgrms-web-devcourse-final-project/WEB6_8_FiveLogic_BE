package com.back.domain.news.like.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.entity.Like;
import com.back.domain.news.like.repository.LikeRepository;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
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
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("사용자는 뉴스에 '좋아요'를 누를 수 있다.")
    void likeNews_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long newsId = news.getId();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(likeRepository.findByMemberAndNews(member, news)).thenReturn(Optional.empty());

        // when
        likeService.likeNews(member, newsId);

        // then
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("존재하지 않는 뉴스에 '좋아요'를 누르면 예외가 발생한다.")
    void likeNews_NewsNotFound_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        Long nonExistentNewsId = 999L;

        when(newsRepository.findById(nonExistentNewsId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            likeService.likeNews(member, nonExistentNewsId);
        });

        assertThat(exception.getMessage()).isEqualTo("해당 뉴스를 찾을 수 없습니다.");
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("이미 '좋아요'를 누른 뉴스에 다시 '좋아요'를 누르면 예외가 발생한다.")
    void likeNews_AlreadyLiked_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long newsId = news.getId();
        Like existingLike = Like.create(member, news);

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));
        when(likeRepository.findByMemberAndNews(member, news)).thenReturn(Optional.of(existingLike));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            likeService.likeNews(member, newsId);
        });

        assertThat(exception.getMessage()).isEqualTo("이미 좋아요를 누른 뉴스입니다.");
        verify(likeRepository, never()).save(any(Like.class));
    }
}
