package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostAllResponse {
    private Long id;
    private String title;
    private String authorName;
    private LocalDateTime createdAt;
    private int viewCount;

    public PostAllResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.authorName = post.getAuthorName();
        this.createdAt = post.getCreateDate();
        this.viewCount = post.getViewCount();
    }

}
