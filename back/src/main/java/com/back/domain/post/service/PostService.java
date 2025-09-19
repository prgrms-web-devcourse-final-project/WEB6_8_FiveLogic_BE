package com.back.domain.post.service;

import com.back.domain.post.dto.PostAllResponse;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
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


    public Post createPost(PostCreateRequest postCreateRequest, String authorName) {
        Post post = new Post();
        post.setTitle(postCreateRequest.getTitle());
        post.setContent(postCreateRequest.getContent());
        post.setAuthorName(authorName);
        String postTypeStr = postCreateRequest.getPostType();
        Post.PostType postType;

        switch(postTypeStr) {
            case "INFORMATIONPOST":
                postType = Post.PostType.INFORMATIONPOST;
                break;
            case "PRACTICEPOST":
                postType = Post.PostType.PRACTICEPOST;
                break;
            case "QUESTIONPOST":
                postType = Post.PostType.QUESTIONPOST;
                break;
            default:
                throw new IllegalArgumentException("Invalid post type: " + postTypeStr);
        }

        post.setPostType(postType);

        postRepository.save(post);

        return post;
    }

    public Post findByid(Long id) {
        return postRepository.findById(id).get();
    }
}
