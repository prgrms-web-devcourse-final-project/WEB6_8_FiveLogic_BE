package com.back.domain.post.post.dto;


import com.back.domain.post.post.entity.Post;
import lombok.Data;

@Data
public class PostDto {
    private Long postId;
    private String title;
    private String content;

    public static PostDto from(Post post) {
        PostDto postDto = new PostDto();
        postDto.setPostId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());

        return postDto;
    }
}
