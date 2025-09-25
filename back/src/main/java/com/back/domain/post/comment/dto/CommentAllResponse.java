package com.back.domain.post.comment.dto;

import com.back.domain.post.comment.entity.PostComment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentAllResponse {
    Long id;
    String content;
    String authorName;
    LocalDateTime createdAt;

    public static CommentAllResponse from(PostComment comment) {
        CommentAllResponse response = new CommentAllResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setAuthorName(comment.getAuthorName()); // Member에서 이름 가져오기
        response.setCreatedAt(comment.getCreateDate());
        return response;
    }
}
