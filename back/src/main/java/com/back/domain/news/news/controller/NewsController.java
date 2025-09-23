package com.back.domain.news.news.controller;

import com.back.domain.file.entity.Video;
import com.back.domain.file.service.VideoService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.like.service.LikeService;
import com.back.domain.news.news.dto.*;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.service.NewsService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
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

    /**
     * 뉴스를 생성합니다.
     * ADMIN 사용자만 접근할 수 있으며 로그인이 필요합니다.
     * title, videoUuid, content 를 요청 바디로 받습니다.
     * rq를 통해 시큐리티 컨텍스트 홀더에 저장된 사용자 정보를 바탕으로 memberService이용, DB에서 Member 엔티티를 조회합니다.
     */
    @PostMapping
    @Operation(summary = "뉴스 생성", description = "뉴스를 생성합니다. ADMIN 사용자만 접근할 수 있습니다.")
    public RsData<NewsCreateResponse> createNews(@RequestBody NewsCreateRequest request) {
        Member member = rq.getActorFromDb().get();
        if (member == null) {
            return new RsData<>("401", "로그인 후 이용해주세요.", null);
        }
        if (member.getRole() != Member.Role.ADMIN) {
            return new RsData<>("403", "권한이 없습니다.", null);
        }
        Video video = videoService.getNewsByUuid(request.videoUuid());
        News news = newsService.createNews(member, request.title(), video, request.content());
        NewsCreateResponse response = new NewsCreateResponse(news.getTitle(), news.getVideo().getUuid(), news.getContent(), member.getName());
        return new RsData<>("201", "뉴스가 생성되었습니다.", response);
    }

    /**
     * 뉴스의 ID를 받아 해당 뉴스를 조회합니다.
     * 모두가 접근할 수 있습니다.
     */
    @GetMapping("{newsId}")
    @Operation(summary = "뉴스 단건 조회", description = "특정 ID의 뉴스를 읽어옵니다.")
    public RsData<NewsGetResponse> getNews(@PathVariable("newsId") Long newsId) {
        News news = newsService.getNewsById(newsId);
        NewsGetResponse response = new NewsGetResponse(news);
        return new RsData<>("200", "뉴스 읽어오기 완료", response);
    }

    /**
     * 뉴스 목록을 페이지 단위로 불러옵니다.
     * 기본 페이지 크기는 10입니다.
     */
    @GetMapping
    @Operation(summary = "뉴스 목록 조회", description = "뉴스 목록을 페이지 단위로 불러옵니다. 기본 페이지 크기는 10입니다.")
    public RsData<List<NewsGetResponse>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<News> newsPage = newsService.getNewsByPage(page, size);

        List<NewsGetResponse> responses = newsPage.getContent().stream()
                .map(NewsGetResponse::new)
                .toList();

        return new RsData<>("200", "뉴스 목록 불러오기 완료", responses);
    }

    /**
     * rq를 통해 시큐리티 컨텍스트 홀더에 저장된 사용자 정보를 바탕으로 memberService이용, DB에서 Member 엔티티를 조회합니다.
     * 로그인한 사용자만 접근할 수 있습니다.
     * 뉴스 ID를 받아 해당 뉴스를 좋아요 처리합니다.
     */
    @PutMapping("{newsId}/likes")
    @Operation(summary = "뉴스 좋아요", description = "특정 ID의 뉴스를 좋아요 합니다. 로그인한 사용자만 접근할 수 있습니다.")
    public RsData<NewsLikeResponse> likeNews(@PathVariable("newsId") Long newsId) {
        Member member = rq.getActorFromDb().get();
        if (member == null) {
            return new RsData<>("401", "로그인 후 이용해주세요.", null);
        }
        try {
            likeService.likeNews(member, newsId);
            NewsLikeResponse response = new NewsLikeResponse(member.getId(), newsId, likeService.getLikeCount(newsId));
            return new RsData<>("200", "뉴스를 좋아합니다.", response);
        } catch (IllegalArgumentException e) {// 현재 존재하지않는 뉴스에 좋아요를 누르려는 경우 IllegalArgumentException를 예외처리합니다. NoSuchElementException로 처리하여 GlobalExceptonHandler에서 처리되도록 할 예정입니다.
            return new RsData<>("404", e.getMessage(), null);
        }
    }

    /**
     * 뉴스를 수정합니다.
     * ADMIN 사용자만 접근할 수 있으며 로그인이 필요합니다.
     * title, videoUuid, content 를 요청 바디로 받습니다.
     * rq를 통해 시큐리티 컨텍스트 홀더에 저장된 사용자 정보를 바탕으로 memberService이용, DB에서 Member 엔티티를 조회합니다.
     */
    @PutMapping("{newsId}")
    @Operation(summary = "뉴스 수정", description = "특정 ID의 뉴스를 수정합니다. ADMIN 사용자만 접근할 수 있습니다.")
    public RsData<NewsUpdateResponse> modifyNews(@PathVariable("newsId") Long newsId, @RequestBody NewsUpdateRequest request) {
        Member member = rq.getActorFromDb().get();
        if (member == null) {
            return new RsData<>("401", "로그인 후 이용해주세요.", null);
        }
        if (member.getRole() != Member.Role.ADMIN) {
            return new RsData<>("403", "권한이 없습니다.", null);
        }
        try {
            News news = newsService.getNewsById(newsId);
            Video video = videoService.getNewsByUuid(request.videoUuid());
            News updatedNews = newsService.updateNews(member, news, request.title(), video, request.content());
            NewsUpdateResponse response = new NewsUpdateResponse(updatedNews);
            return new RsData<>("200", "뉴스가 수정되었습니다.", response);
        } catch (IllegalArgumentException e) {
            return new RsData<>("404", e.getMessage(), null);
        }
    }

    /**
     * 뉴스를 삭제합니다.
     * ADMIN 사용자만 접근할 수 있으며 로그인이 필요합니다.
     */
    @DeleteMapping("{newsId}")
    @Operation(summary = "뉴스 삭제", description = "특정 ID의 뉴스를 삭제합니다. ADMIN 사용자만 접근할 수 있습니다.")
    public RsData<?> deleteNews(@PathVariable("newsId") Long newsId) {
        Member member = rq.getActorFromDb().get();
        if (member == null){
            return new RsData<>("401", "로그인 후 이용해주세요.", null);
        }
        if (member.getRole() != Member.Role.ADMIN) {
            return new RsData<>("403", "권한이 없습니다.", null);
        }
        News news = newsService.getNewsById(newsId);
        newsService.deleteNews(member, news);
        return new RsData<>("200", newsId + "번 뉴스가 삭제되었습니다.", null);
    }
}
