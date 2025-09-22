package com.back.domain.news.comment.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.dto.CommentCreateRequest;
import com.back.domain.news.comment.dto.CommentResponse;
import com.back.domain.news.comment.dto.CommentUpdateRequest;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.comment.service.CommentService;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.service.NewsService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/news/{newsId}/comment")
@RequiredArgsConstructor
public class CommentController {
    private final NewsService newsService;
    private final CommentService commentService;
    private final Rq rq;

    @GetMapping
    public RsData<List<CommentResponse>> getComments(@PathVariable Long newsId) {
        News news = newsService.getNewsById(newsId);
        List<Comment> comments = commentService.getComments(news);
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        return new RsData<>("200", "댓글 목록 불러오기 완료", commentResponses);
    }

    @PostMapping
    public RsData<CommentResponse> createComment(@PathVariable Long newsId, @RequestBody CommentCreateRequest request) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        News news = newsService.getNewsById(newsId);
        Comment comment = commentService.createComment(member, news, request.content());
        CommentResponse commentResponse = new CommentResponse(comment);
        return new RsData<>("201", "댓글 생성 완료", commentResponse);
    }

    @PutMapping("/{commentId}") // Updated mapping
    public RsData<CommentResponse> updateComment(@PathVariable Long newsId, @PathVariable Long commentId, @RequestBody CommentUpdateRequest request) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        Comment updatedComment = commentService.updateComment(member, commentId, request.content());
        CommentResponse commentResponse = new CommentResponse(updatedComment);
        return new RsData<>("200", "댓글 수정 완료", commentResponse);
    }

    @DeleteMapping("/{commentId}") // Updated mapping
    public RsData<Void> deleteComment(@PathVariable Long newsId, @PathVariable Long commentId) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        commentService.deleteComment(member, commentId);
        return new RsData<>("200", "댓글 삭제 완료");
    }
}
