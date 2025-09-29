package com.back.domain.news.comment.dto;

import com.back.domain.news.comment.entity.NewsComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private final Long id;
    private final String content;
    private final String author;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;

    public CommentResponse(NewsComment newsComment) {
        this.id = newsComment.getId();
        this.content = newsComment.getContent();
        this.author = newsComment.getMember().getName();
        this.createdDate = newsComment.getCreateDate();
        this.modifiedDate = newsComment.getModifyDate();
    }
}
