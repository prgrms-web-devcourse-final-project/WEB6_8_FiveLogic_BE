package com.back.domain.news.news.controller;

import com.back.domain.file.entity.Video;
import com.back.domain.file.service.VideoService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.dto.NewsCreateRequest;
import com.back.domain.news.news.dto.NewsCreateResponse;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.service.NewsService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private final VideoService videoService;
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
    public void getNews() {

    }

    @GetMapping
    public void getNewsList() {

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
