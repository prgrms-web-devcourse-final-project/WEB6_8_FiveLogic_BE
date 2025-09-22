package com.back.domain.post.service;

import com.back.domain.post.dto.PostAllResponse;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.entity.Post;
import com.back.domain.post.entity.PracticePost;
import com.back.domain.post.entity.QuestionPost;
import com.back.domain.post.repository.PostRepository;
import com.back.domain.post.repository.PracticePostRepository;
import com.back.domain.post.repository.QuestionPostRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PracticePostRepository practicePostRepository;
    private final QuestionPostRepository questionPostRepository;

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts;
    }


    public Post createPost(PostCreateRequest postCreateRequest, String authorName) {
        String postTypeStr = postCreateRequest.getPostType();
        validPostType(postTypeStr);

        Post.PostType postType = Post.PostType.valueOf(postTypeStr);

        Post post = createPostByType(postType);
        post.setTitle(postCreateRequest.getTitle());
        post.setContent(postCreateRequest.getContent());
        post.setAuthorName(authorName);
        post.setPostType(postType);

        return saveByType(post);
    }

    private Post createPostByType(Post.PostType postType) {
        return switch (postType) {
            case INFORMATIONPOST -> new Post();
            case PRACTICEPOST -> new PracticePost();
            case QUESTIONPOST -> new QuestionPost();
        };
    }

    private Post saveByType(Post post) {
        return switch (post.getPostType()) {
            case INFORMATIONPOST -> postRepository.save(post);
            case PRACTICEPOST -> practicePostRepository.save((PracticePost) post);
            case QUESTIONPOST -> questionPostRepository.save((QuestionPost) post);
        };
    }

    private void validPostType(String postTypeStr) {
        boolean eq = false;

        String[] validType = new String[3];
        validType[0] = "INFORMATIONPOST";
        validType[1] = "PRACTICEPOST";
        validType[2] = "QUESTIONPOST";

        for(String x : validType) if(x.equals(postTypeStr)) eq = true;

        if(!eq) throw new ServiceException("400-2", "유효하지 않은 PostType입니다.");
    }


    public Post findById(Long id, String category) {
        Post.PostType postType = Post.PostType.valueOf(category);
        return switch (postType) {
            case INFORMATIONPOST -> postRepository.findById(id).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
            case PRACTICEPOST -> practicePostRepository.findById(id).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
            case QUESTIONPOST -> questionPostRepository.findById(id).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
        };
    }

    public List<PostAllResponse> getAllPostResponse() {
        return postRepository.findAll().stream()
                .map(PostAllResponse::new)
                .toList();
    }
}
