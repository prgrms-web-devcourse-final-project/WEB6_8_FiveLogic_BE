package com.back.domain.Post.controller;

import com.back.domain.Post.dto.PostAllResponse;
import com.back.domain.Post.rq.ApiResponse;
import com.back.domain.Post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("post/infor")
@RequiredArgsConstructor
public class InformationPostController {
    private final PostService postService;



    @GetMapping
    public ResponseEntity<ApiResponse<List<PostAllResponse>>> getAllPost() {
        List<PostAllResponse> posts = postService.getAllPosts();
        ApiResponse<List<PostAllResponse>> response = new ApiResponse<>("게시글 조회 성공", posts);
        return ResponseEntity.ok(response);
    }

}
