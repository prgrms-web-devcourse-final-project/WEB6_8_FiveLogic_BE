package com.back.domain.post.post.dto;

import com.back.domain.post.post.entity.Post;
import lombok.Data;


public record PostCreateResponse(
        Long postId,
        String title) {


    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(post.getId(), post.getTitle());
    }
}
