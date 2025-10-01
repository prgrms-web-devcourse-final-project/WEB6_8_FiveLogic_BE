package com.back.domain.post.post.dto;

import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createdAt,
        int viewCount,

        int likeCount,
        int dislikeCount,
        String userLikeStatus,

        List<CommentAllResponse> comments) {





    public static PostDetailResponse from(
            Post post,
            List<CommentAllResponse> comments,
            int likeCount,
            int dislikeCount,
            String userLikeStatus
    ) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorName(),
                post.getCreateDate(),
                post.getViewCount(),
                likeCount,
                dislikeCount,
                userLikeStatus,
                comments
        );
    }

}
