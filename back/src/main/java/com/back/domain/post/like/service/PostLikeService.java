package com.back.domain.post.like.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.like.entity.PostLike;
import com.back.domain.post.like.repository.PostLikeRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rq.Rq;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final Rq rq;
    private final PostLikeRepository postLikeRepository;
    private final PostService postService;


    @Transactional
    public void likePost(long postId) {
        Member member = rq.getActor();
        Post post = postService.findById(postId);

        // 기존 좋아요/싫어요 기록 조회
        Optional<PostLike> existingLike = postLikeRepository.findByMemberAndPost(member, post);

        if (existingLike.isPresent()) {
            PostLike postLike = existingLike.get();

            if (postLike.getStatus() == PostLike.LikeStatus.LIKE) {
                // 이미 좋아요 상태 -> 좋아요 취소
                postLikeRepository.delete(postLike);
            } else {
                // 싫어요 상태 -> 좋아요로 변경
                postLike.updateStatus(PostLike.LikeStatus.LIKE);
            }
        } else {
            // 처음 좋아요
            PostLike newLike = PostLike.create(member, post, PostLike.LikeStatus.LIKE);
            postLikeRepository.save(newLike);

        }
    }

    @Transactional
    public void disLikePost(long postId) {
        Member member = rq.getActor();
        Post post = postService.findById(postId);

        // 기존 좋아요/싫어요 기록 조회
        Optional<PostLike> existingLike = postLikeRepository.findByMemberAndPost(member, post);

        if (existingLike.isPresent()) {
            PostLike postLike = existingLike.get();

            if (postLike.getStatus() == PostLike.LikeStatus.DISLIKE) {
                // 이미 싫어요 상태 -> 싫어요 취소
                postLikeRepository.delete(postLike);
            } else {
                // 좋아요 상태 -> 싫어요로 변경
                postLike.updateStatus(PostLike.LikeStatus.DISLIKE);
            }
        } else {
            // 처음 싫어요
            PostLike newDislike = PostLike.create(member, post, PostLike.LikeStatus.DISLIKE);
            postLikeRepository.save(newDislike);
        }
    }

    public int showDisLikeCount(Long postId) {
        return postLikeRepository.countDislikesByPostId(postId);
    }

    public int showLikeCount(Long postId) {
        return postLikeRepository.countLikesByPostId(postId);
    }
}
