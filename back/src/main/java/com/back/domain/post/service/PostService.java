package com.back.domain.post.service;

import com.back.domain.post.dto.PostAllResponse;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts;
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
                throw new ServiceException("400-2", "유효하지 않은 PostType입니다.");
        }

        post.setPostType(postType);

        postRepository.save(post);

        return post;
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
    }

    public List<PostAllResponse> getAllPostResponse() {
        return postRepository.findAll().stream()
                .map(PostAllResponse::new)
                .toList();
    }
}
