package com.back.domain.Post.service;

import com.back.domain.Post.dto.PostAllResponse;
import com.back.domain.Post.entity.Post;
import com.back.domain.Post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<PostAllResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts.stream()
                .map(PostAllResponse::new)
                .collect(Collectors.toList());
    }


}
