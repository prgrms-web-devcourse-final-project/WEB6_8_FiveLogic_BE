package com.back.domain.news.comment.controller;

import com.back.domain.news.news.service.NewsService;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news/{newsId}/comment")
@RequiredArgsConstructor
public class CommentController {
    private final NewsService newsService;
    private final Rq rq;

    @GetMapping
    public void getComments(@PathVariable Long newsId) {
        // ... 로직 구현
    }

    @PostMapping
    public void createComment(@PathVariable Long newsId) {
        // ... 로직 구현
    }

    @PutMapping
    public void updateComment(@PathVariable Long newsId) {
        // ... 로직 구현
    }

    @DeleteMapping
    public void deleteComment(@PathVariable Long newsId) {
        // ... 로직 구현
    }
}
