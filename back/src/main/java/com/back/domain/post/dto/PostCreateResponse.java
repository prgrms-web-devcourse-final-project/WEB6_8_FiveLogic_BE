package com.back.domain.post.dto;

import com.back.domain.post.entity.Post;
import lombok.Data;

@Data
public class PostCreateResponse {
    private Long postId;
    private String title;

    public static PostCreateResponse from(Post post) {
        PostCreateResponse response = new PostCreateResponse();
        response.setPostId(post.getId());
        response.setTitle(post.getTitle());
        return response;
    }
}
