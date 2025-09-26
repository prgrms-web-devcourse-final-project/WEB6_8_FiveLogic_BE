package com.back.domain.post.post.dto;

import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.post.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class PostDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private int viewCount;

    private int likeCount;
    private int dislikeCount;
    private String userLikeStatus;

    private List<CommentAllResponse> comments;



    public static PostDetailResponse from(Post post, List<CommentAllResponse> comments, int likeCount, int dislikeCount, String userLikeStatus) {
        PostDetailResponse postDetailResponse = new PostDetailResponse();

        postDetailResponse.setId(post.getId());
        postDetailResponse.setTitle(post.getTitle());
        postDetailResponse.setContent(post.getContent());
        postDetailResponse.setAuthorName(post.getAuthorName());
        postDetailResponse.setCreatedAt(post.getCreateDate());
        postDetailResponse.setViewCount(post.getViewCount());

        postDetailResponse.setLikeCount(likeCount);
        postDetailResponse.setDislikeCount(dislikeCount);
        postDetailResponse.setUserLikeStatus(userLikeStatus);

        postDetailResponse.setComments(comments);



        return postDetailResponse;
    }

}
