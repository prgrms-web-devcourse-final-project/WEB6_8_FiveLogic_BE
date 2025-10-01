package com.back.domain.post.post.dto;


import com.back.domain.post.post.entity.Post;



public record PostDto(
        Long postId,
        String title,
        String content,
        int viewCount
) {


    public static PostDto from(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount()
        );
    }
}
