package com.back.domain.news.news.service;

import com.back.domain.file.video.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
import com.back.fixture.VideoFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsServiceTest {
    @Mock
    private NewsRepository newsRepository;
    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("뉴스 생성 성공")
    void createNews_Success() {
        // given
        Member member = MemberFixture.createDefault();
        Video video = VideoFixture.createDefault();
        String title = "테스트 뉴스";
        String content = "테스트 뉴스 내용입니다.";
        News news = News.create(member, title, video, content);

        when(newsRepository.save(any(News.class))).thenReturn(news);

        // when
        News createdNews = newsService.createNews(member, title, video, content);

        // then
        assertThat(createdNews).isNotNull();
        assertThat(createdNews.getTitle()).isEqualTo(title);
        assertThat(createdNews.getContent()).isEqualTo(content);
        assertThat(createdNews.getMember()).isEqualTo(member);
        assertThat(createdNews.getVideo()).isEqualTo(video);
        verify(newsRepository, times(1)).save(any(News.class));
    }

    @Test
    @DisplayName("뉴스 목록 페이지별 조회 성공")
    void getNewsByPage_Success() {
        // given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        Page<News> newsPage = new PageImpl<>(Collections.singletonList(NewsFixture.createDefault()));

        when(newsRepository.findAll(pageable)).thenReturn(newsPage);

        // when
        Page<News> result = newsService.getNewsByPage(page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(newsRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("ID로 뉴스 조회 성공")
    void getNewsById_Success() {
        // given
        Long newsId = 1L;
        News news = NewsFixture.createDefault();
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(news));

        // when
        News foundNews = newsService.getNewsById(newsId);

        // then
        assertThat(foundNews).isNotNull();
        assertThat(foundNews).isEqualTo(news);
        verify(newsRepository, times(1)).findById(newsId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 뉴스 조회 시 예외 발생")
    void getNewsById_NotFound_ThrowsException() {
        // given
        Long nonExistentNewsId = 999L;
        when(newsRepository.findById(nonExistentNewsId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ServiceException.class, () -> {
            newsService.getNewsById(nonExistentNewsId);
        });
        verify(newsRepository, times(1)).findById(nonExistentNewsId);
    }

    @Test
    @DisplayName("뉴스 수정 성공 (관리자)")
    void updateNews_Admin_Success() {
        // given
        Member admin = MemberFixture.create(1L, "admin@example.com", "Admin", "adminpass", Member.Role.ADMIN);
        News news = NewsFixture.createDefault();
        Video newVideo = VideoFixture.createDefault();
        String newTitle = "수정된 뉴스 제목";
        String newContent = "수정된 뉴스 내용";

        when(newsRepository.save(any(News.class))).thenReturn(news);

        // when
        News updatedNews = newsService.updateNews(admin, news, newTitle, newVideo, newContent);

        // then
        assertThat(updatedNews).isNotNull();
        assertThat(updatedNews.getTitle()).isEqualTo(newTitle);
        assertThat(updatedNews.getContent()).isEqualTo(newContent);
        assertThat(updatedNews.getVideo()).isEqualTo(newVideo);
        verify(newsRepository, times(1)).save(news);
    }

    @Test
    @DisplayName("뉴스 수정 실패 (권한 없음)")
    void updateNews_NoPermission_ThrowsException() {
        // given
        Member user = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Video newVideo = VideoFixture.createDefault();
        String newTitle = "수정된 뉴스 제목";
        String newContent = "수정된 뉴스 내용";

        // when & then
        assertThrows(ServiceException.class, () -> {
            newsService.updateNews(user, news, newTitle, newVideo, newContent);
        });
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    @DisplayName("뉴스 삭제 성공 (관리자)")
    void deleteNews_Admin_Success() {
        // given
        Member admin = MemberFixture.create(1L, "admin@example.com", "Admin", "adminpass", Member.Role.ADMIN);
        News news = NewsFixture.createDefault();
        doNothing().when(newsRepository).delete(news);

        // when
        newsService.deleteNews(admin, news);

        // then
        verify(newsRepository, times(1)).delete(news);
    }

    @Test
    @DisplayName("뉴스 삭제 실패 (권한 없음)")
    void deleteNews_NoPermission_ThrowsException() {
        // given
        Member user = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();

        // when & then
        assertThrows(ServiceException.class, () -> {
            newsService.deleteNews(user, news);
        });
        verify(newsRepository, never()).delete(any(News.class));
    }
}