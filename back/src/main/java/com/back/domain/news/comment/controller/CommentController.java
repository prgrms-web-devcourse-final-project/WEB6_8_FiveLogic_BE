package com.back.domain.news.comment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.dto.CommentCreateRequest;
import com.back.domain.news.comment.dto.CommentResponse;
import com.back.domain.news.comment.dto.CommentUpdateRequest;
import com.back.domain.news.comment.entity.NewsComment;
import com.back.domain.news.comment.service.NewsCommentService;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.service.NewsService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/news/{newsId}/comment")
@RequiredArgsConstructor
public class CommentController {
    private final NewsService newsService;
    private final NewsCommentService newsCommentService;
    private final Rq rq;

    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "특정 뉴스의 댓글 목록을 불러옵니다.")
    public RsData<List<CommentResponse>> getComments(@PathVariable Long newsId) {
        News news = newsService.getNewsById(newsId);
        List<NewsComment> newsComments = newsCommentService.getComments(news);
        List<CommentResponse> commentResponses = newsComments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        return new RsData<>("200", "댓글 목록 불러오기 완료", commentResponses);
    }

    @PostMapping
    @Operation(summary = "댓글 생성", description = "특정 뉴스에 댓글을 생성합니다. 로그인한 사용자만 접근할 수 있습니다.")
    public RsData<CommentResponse> createComment(@PathVariable Long newsId, @RequestBody CommentCreateRequest request) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        News news = newsService.getNewsById(newsId);
        NewsComment newsComment = newsCommentService.createComment(member, news, request.content());
        CommentResponse commentResponse = new CommentResponse(newsComment);
        return new RsData<>("201", "댓글 생성 완료", commentResponse);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "특정 뉴스의 댓글을 수정합니다. 댓글 작성자만 접근할 수 있습니다.")
    public RsData<CommentResponse> updateComment(@PathVariable Long newsId, @PathVariable Long commentId, @RequestBody CommentUpdateRequest request) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        try {
            News news = newsService.getNewsById(newsId);
            NewsComment updatedNewsComment = newsCommentService.updateComment(member, news, commentId, request.content());
            CommentResponse commentResponse = new CommentResponse(updatedNewsComment);
            return new RsData<>("200", "댓글 수정 완료", commentResponse);
        } catch (AccessDeniedException e) {
            return new RsData<>("403", e.getMessage());
        } catch (IllegalArgumentException e) {
            return new RsData<>("400", e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 뉴스의 댓글을 삭제합니다. 댓글 작성자만 접근할 수 있습니다.")
    public RsData<Void> deleteComment(@PathVariable Long newsId, @PathVariable Long commentId) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        try {
            News news = newsService.getNewsById(newsId);
            newsCommentService.deleteComment(member, news, commentId);
            return new RsData<>("200", "댓글 삭제 완료");
        } catch (AccessDeniedException e) {
            return new RsData<>("403", e.getMessage());
        } catch (IllegalArgumentException e) {
            return new RsData<>("400", e.getMessage());
        }
    }
}
