package com.back.domain.news.comment.dto;

import com.back.domain.news.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private final Long id;
    private final String content;
    private final String author;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.author = comment.getMember().getName();
        this.createdDate = comment.getCreateDate();
        this.modifiedDate = comment.getModifyDate();
    }
}
