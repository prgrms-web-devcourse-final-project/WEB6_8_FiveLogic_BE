package com.back.domain.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.dto.PostAllResponse;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.dto.PostCreateResponse;
import com.back.domain.post.entity.Post;
import com.back.domain.post.rq.ApiResponse;
import com.back.domain.post.service.PostService;
import com.back.global.auth.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("post/infor")
@RequiredArgsConstructor
public class InformationPostController {
    private final PostService postService;


    @Operation(summary = "게시글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @RequestBody PostCreateRequest postCreateRequest,
            @CurrentUser Member member
            ) {
        String authorName = member.getName();
        Post post = postService.createPost(postCreateRequest, authorName);
        PostCreateResponse postCreateResponse = PostCreateResponse.from(post);
        ApiResponse<PostCreateResponse> response = new ApiResponse<>("게시글이 성공적으로 생성되었습니다. " , postCreateResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 다건 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostAllResponse>>> getAllPost() {
        List<PostAllResponse> posts = postService.getAllPosts();
        ApiResponse<List<PostAllResponse>> response = new ApiResponse<>("게시글 조회 성공", posts);
        return ResponseEntity.ok(response);
    }
}
