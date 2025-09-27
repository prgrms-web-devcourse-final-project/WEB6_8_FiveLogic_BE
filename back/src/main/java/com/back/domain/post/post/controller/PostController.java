package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.like.service.PostLikeService;
import com.back.domain.post.post.dto.*;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.rq.PostDetailFacade;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostLikeService postLikeService;
    private final PostService postService;
    private final Rq rq;
    private final PostDetailFacade postDetailFacade;


    @Operation(summary = "게시글 조회 - 페이징 처리")
    @GetMapping("/page/{postType}")
    public RsData<PostPagingResponse> getPostWithPage(
            @PathVariable Post.PostType postType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Page<PostDto> postPage = postService.getPosts(keyword, page,size, postType);
        PostPagingResponse resDto = PostPagingResponse.from(postPage);

        return new RsData<>("200", "게시글이 조회 되었습니다.", resDto);
    }

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
    @GetMapping("/all")
    public RsData<List<PostAllResponse>> getAllPost() {
        List<PostAllResponse> postAllResponse = postService.getAllPostResponse();


        return new RsData<>("200", "게시글 다건 조회 성공", postAllResponse);
    }

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{post_id}")
    public RsData<PostSingleResponse> getSinglePost(@PathVariable Long post_id) {
        Post post = postService.findById(post_id);

        PostSingleResponse postSingleResponse = new PostSingleResponse(post);

        return new RsData<>("200", "게시글 단건 조회 성공", postSingleResponse);
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{post_id}")
    public RsData<Void> removePost(@PathVariable Long post_id) {
        Member member = rq.getActor();

        postService.removePost(post_id, member);

        return new RsData<>("200", "게시글 삭제 성공", null);
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{post_id}")
    public RsData<Void> updatePost(@PathVariable Long post_id
            ,@Valid @RequestBody PostCreateRequest postCreateRequest) {
        Member member = rq.getActor();
        postService.updatePost(post_id, member, postCreateRequest);

        return new RsData<>("200", "게시글 수정 성공", null);
    }

    @Operation(summary = "게시글 좋아요")
    @PostMapping("/{post_id}/liked")
    public RsData<Void> likePost(@PathVariable Long post_id) {
        postLikeService.likePost(post_id);

        return new RsData<>("200", "게시글 좋아요 성공", null);
    }

    @Operation(summary = "게시글 좋아요 (Show)")
    @GetMapping("/{post_id}/liked")
    public RsData<PostLikedResponse> getLike(@PathVariable Long post_id) {
        int likeCount = postLikeService.getLikeCount(post_id);
        PostLikedResponse postLikedResponse = new PostLikedResponse(likeCount);

        return new RsData<>("200", "게시글 좋아요 조회 성공", postLikedResponse);
    }

    @Operation(summary = "게시글 싫어요 (Show)")
    @GetMapping("/{post_id}/disliked")
    public RsData<PostLikedResponse> getDisLike(@PathVariable Long post_id) {
        int likeCount = postLikeService.getDisLikeCount(post_id);
        PostLikedResponse postLikedResponse = new PostLikedResponse(likeCount);

        return new RsData<>("200", "게시글 싫어요 조회 성공", postLikedResponse);
    }

    @Operation(summary = "게시글 싫어요")
    @PostMapping("/{post_id}/disliked")
    public RsData<PostLikedResponse> disLikePost(@PathVariable Long post_id) {
        postLikeService.disLikePost(post_id);

        return new RsData<>("200", "게시글 싫어요 성공", null);
    }


    @Operation(summary = "게시글 상세페이지")
    @GetMapping("/Detail/{post_id}")
    public RsData<PostDetailResponse> getPostDetail(@PathVariable Long post_id) {

        PostDetailResponse response = postDetailFacade.getDetailWithViewIncrement(post_id);

        return new RsData<>("200", "게시글 상세 조회 성공", response);
    }
}
