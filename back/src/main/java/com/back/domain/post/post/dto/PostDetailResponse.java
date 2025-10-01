package com.back.domain.post.post.dto;

import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.post.entity.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
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



    public static PostDetailResponse from(
            Post post,
            List<CommentAllResponse> comments,
            int likeCount,
            int dislikeCount,
            String userLikeStatus
    ) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getAuthorName())
                .createdAt(post.getCreateDate())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userLikeStatus(userLikeStatus)
                .comments(comments)
                .build();
    }

}
