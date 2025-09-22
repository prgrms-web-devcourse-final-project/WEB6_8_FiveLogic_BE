package com.back.domain.news.news.controller;

import com.back.domain.file.entity.Video;
import com.back.domain.file.service.VideoService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.service.LikeService;
import com.back.domain.news.news.dto.NewsCreateRequest;
import com.back.domain.news.news.dto.NewsCreateResponse;
import com.back.domain.news.news.dto.NewsGetResponse;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.service.NewsService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private final VideoService videoService;
    private final LikeService likeService;
    private final Rq rq;

    @PostMapping
    public RsData<NewsCreateResponse> createNews(@RequestBody NewsCreateRequest request) {
        Member member = rq.getActor();
        Video video = videoService.findByUuid(request.videoUuid());
        News news = newsService.createNews(member, request.title(), video, request.content());
        NewsCreateResponse response = new NewsCreateResponse(news.getTitle(), news.getVideo().getUuid(), news.getContent(), member.getName());
        return new RsData<>("201", "뉴스가 생성되었습니다.", response);
    }

    @GetMapping("{newsId}")
    public RsData<NewsGetResponse> getNews(@PathVariable Integer newsId) {
        News news = newsService.getNewsById(newsId);
        NewsGetResponse response = new NewsGetResponse(news);
        return new RsData<>("200", "뉴스 읽어오기 완료", response);
    }

    @GetMapping
    public RsData<List<NewsGetResponse>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<News> newsPage = newsService.getNewsByPage(page, size);

        List<NewsGetResponse> responses = newsPage.getContent().stream()
                .map(NewsGetResponse::new)
                .toList();

        return new RsData<>("200", "뉴스 목록 불러오기 완료", responses);
    }

    @PutMapping
    public void likeNews() {

    }

    @PutMapping
    public void modifyNews() {

    }

    @DeleteMapping
    public void deleteNews() {

    }
}
