package com.back.domain.news.news.service;


import com.back.domain.file.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public News getNewsById(Integer id) {
        return newsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다."));
    }

    public void updateNews(News news, String title, Video video, String content) {
        news.update(title, video, content);
        newsRepository.save(news);
    }

    public void deleteNews(News news) {
        newsRepository.delete(news);
    }
}
