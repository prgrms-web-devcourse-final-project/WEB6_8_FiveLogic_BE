package com.back.domain.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.dto.PostAllResponse;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.dto.PostCreateResponse;
import com.back.domain.post.dto.PostSingleResponse;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
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
            @Valid @RequestBody PostCreateRequest postCreateRequest,
            @CurrentUser Member member
            ) {

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
    public RsData<PostSingleResponse> removePost(@PathVariable long post_id, @CurrentUser Member member) {

        postService.removePost(post_id, member);

        return new RsData<>("200", "게시글 삭제 성공", null);
    }
}
