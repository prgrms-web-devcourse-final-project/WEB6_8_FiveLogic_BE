package com.back.domain.news.news.service;


import com.back.domain.file.video.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public News createNews(Member member, String title, Video video, String content) {
        News news = News.create(member, title, video, content);
        return newsRepository.save(news);
    }

    public Page<News> getNewsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        return newsRepository.findAll(pageable);
    }

    public News getNewsById(Long id) {
        return newsRepository.findById(id).orElseThrow(() -> new NoSuchElementException("뉴스를 찾을 수 없습니다."));
    }

    public News updateNews(Member member, News news, String title, Video video, String content) {
        if (!(member.getRole()== Member.Role.ADMIN)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }
        news.update(title, video, content);
        return newsRepository.save(news);
    }

    public void deleteNews(Member member, News news) {
        if (!(member.getRole()== Member.Role.ADMIN)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        newsRepository.delete(news);
    }
}
