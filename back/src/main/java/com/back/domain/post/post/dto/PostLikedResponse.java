package com.back.domain.post.post.dto;

import lombok.Data;

@Data
public class PostLikedResponse {
    private int likeCount;

    public PostLikedResponse(int likeCount) {
        this.likeCount = likeCount;
    }
}
