package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.*;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.auth.CurrentUser;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("post/infor")
@RequiredArgsConstructor
public class InformationPostController {
    private final PostService postService;
    private final Rq rq;


    @Operation(summary = "게시글 생성")
    @PostMapping
    public RsData<PostCreateResponse> createPost(
            @Valid @RequestBody PostCreateRequest postCreateRequest
            ) {
        Member member = rq.getActor();
        Post post = postService.createPost(postCreateRequest, member);
        PostCreateResponse postCreateResponse = PostCreateResponse.from(post);

        return new RsData<>("200", "게시글이 성공적으로 생성되었습니다.", postCreateResponse);
    }

    @Operation(summary = "게시글 다건 조회")
    @GetMapping
    public RsData<List<PostAllResponse>> getAllPost() {
        List<PostAllResponse> postAllResponse = postService.getAllPostResponse();


        return new RsData<>("200", "게시글 다건 조회 성공", postAllResponse);
    }

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{post_id}")
    public RsData<PostSingleResponse> getSinglePost(@PathVariable long post_id) {
        Post post = postService.findById(post_id);

        PostSingleResponse postSingleResponse = new PostSingleResponse(post);

        return new RsData<>("200", "게시글 단건 조회 성공", postSingleResponse);
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{post_id}")
    public RsData<Void> removePost(@PathVariable long post_id) {
        Member member = rq.getActor();

        postService.removePost(post_id, member);

        return new RsData<>("200", "게시글 삭제 성공", null);
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{post_id}")
    public RsData<Void> updatePost(@PathVariable long post_id
            ,@CurrentUser Member member
            ,@Valid @RequestBody PostCreateRequest postCreateRequest) {

        postService.updatePost(post_id, member, postCreateRequest);

        return new RsData<>("200", "게시글 수정 성공", null);
    }

    @Operation(summary = "게시글 좋아요 + ")
    @PostMapping("/{post_id}/liked")
    public RsData<Void> likePost(@PathVariable long post_id) {
        postService.likePost(post_id);

        return new RsData<>("200", "게시글 좋아요 성공", null);
    }

    @Operation(summary = "게시글 좋아요 (Show)")
    @GetMapping("/{post_id}/liked")
    public RsData<PostLikedResponse> getlike(@PathVariable long post_id) {
        int likeCount = postService.showLikeCount(post_id);
        PostLikedResponse postLikedResponse = new PostLikedResponse(likeCount);

        return new RsData<>("200", "게시글 좋아요 조회 성공", postLikedResponse);
    }

    @Operation(summary = "게시글 싫어요")
    @PostMapping("/{post_id}/disliked")
    public RsData<PostLikedResponse> disLikePost(@PathVariable long post_id) {
        postService.disLikePost(post_id);

        return new RsData<>("200", "게시글 싫어요 성공", null);
    }
}
