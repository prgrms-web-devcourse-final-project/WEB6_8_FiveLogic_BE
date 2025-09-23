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
    private final CommentService commentService;
    private final Rq rq;

    /**
     * 특정 뉴스의 댓글 목록을 조회합니다. 모두가 접근할 수 있습니다.
     */
    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "특정 뉴스의 댓글 목록을 불러옵니다.")
    public RsData<List<CommentResponse>> getComments(@PathVariable Long newsId) {
        News news = newsService.getNewsById(newsId);
        List<Comment> comments = commentService.getComments(news);
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        return new RsData<>("200", "댓글 목록 불러오기 완료", commentResponses);
    }

    /**
     * 특정 뉴스에 댓글을 생성합니다. 로그인한 사용자만 접근할 수 있습니다.
     */
    @PostMapping
    @Operation(summary = "댓글 생성", description = "특정 뉴스에 댓글을 생성합니다. 로그인한 사용자만 접근할 수 있습니다.")
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

    /**
     * 특정 뉴스의 댓글을 수정합니다. 댓글 작성자만 접근할 수 있습니다.
     */
    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "특정 뉴스의 댓글을 수정합니다. 댓글 작성자만 접근할 수 있습니다.")
    public RsData<CommentResponse> updateComment(@PathVariable Long newsId, @PathVariable Long commentId, @RequestBody CommentUpdateRequest request) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        try {
            News news = newsService.getNewsById(newsId);
            Comment updatedComment = commentService.updateComment(member, news, commentId, request.content());
            CommentResponse commentResponse = new CommentResponse(updatedComment);
            return new RsData<>("200", "댓글 수정 완료", commentResponse);
        } catch (AccessDeniedException e) {
            return new RsData<>("403", e.getMessage());
        } catch (IllegalArgumentException e) {
            return new RsData<>("400", e.getMessage());
        }
    }

    /**
     * 특정 뉴스의 댓글을 삭제합니다. 댓글 작성자만 접근할 수 있습니다.
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "특정 뉴스의 댓글을 삭제합니다. 댓글 작성자만 접근할 수 있습니다.")
    public RsData<Void> deleteComment(@PathVariable Long newsId, @PathVariable Long commentId) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401", "로그인이 필요합니다.");
        }
        try {
            News news = newsService.getNewsById(newsId);
            commentService.deleteComment(member, news, commentId);
            return new RsData<>("200", "댓글 삭제 완료");
        } catch (AccessDeniedException e) {
            return new RsData<>("403", e.getMessage());
        } catch (IllegalArgumentException e) {
            return new RsData<>("400", e.getMessage());
        }
    }
}
