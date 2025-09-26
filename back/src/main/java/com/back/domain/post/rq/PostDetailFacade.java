package com.back.domain.post.rq;

import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.service.PostCommentService;
import com.back.domain.post.like.service.PostLikeService;
import com.back.domain.post.post.dto.PostDetailResponse;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostDetailFacade {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostCommentService postCommentService;

    @Transactional
    public PostDetailResponse getDetailWithViewIncrement(Long postId) {
        // 조회수 증가와 상세정보 조회를 한 번에
        Post post = postService.getPostDetailWithViewIncrement(postId);

        List<CommentAllResponse> comments = postCommentService.getAllPostCommentResponse(postId);
        int likeCount = postLikeService.getLikeCount(postId);
        int dislikeCount = postLikeService.getDisLikeCount(postId);
        String userStatus = postLikeService.getPresentStatus(postId);

        return PostDetailResponse.from(post, comments, likeCount, dislikeCount, userStatus);
    }

//    public Post findById(Long postId) {
//        return postService.findById(postId);
//    }


}
