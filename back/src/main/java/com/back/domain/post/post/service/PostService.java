package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.PostAllResponse;
import com.back.domain.post.post.dto.PostCreateRequest;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
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


    public Post createPost(PostCreateRequest postCreateRequest, Member member) {
        String postTypeStr = postCreateRequest.getPostType();

        validPostType(postTypeStr);



        Post.PostType postType = Post.PostType.valueOf(postTypeStr);

        Post post = new Post();
        post.setTitle(postCreateRequest.getTitle());
        post.setContent(postCreateRequest.getContent());
        post.setAuthorName(member.getName());
        post.setMember(member);
        post.setPostType(postType);

        postRepository.save(post);

        return post;
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


    public Post findById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
        return post;
    }

    public List<PostAllResponse> getAllPostResponse() {
        return postRepository.findAll().stream()
                .map(PostAllResponse::new)
                .toList();
    }

    public void removePost(Long postId, Member member) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
        Long authorId = post.getMember().getId();
        if(authorId != member.getId()) throw new ServiceException("400", "삭제 권한이 없습니다.");

        postRepository.delete(post);
    }

    public void updatePost(long postId, Member member, @Valid PostCreateRequest postCreateRequest) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
        Long authorId = post.getMember().getId();
        if(authorId != member.getId()) throw new ServiceException("400", "수정 권한이 없습니다.");

        post.setTitle(postCreateRequest.getTitle());
        post.setContent(postCreateRequest.getContent());

        postRepository.save(post);
    }

    public void likePost(long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        post.setLiked(post.getLiked()+1);
    }

    public void disLikePost(long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        post.setLiked(post.getLiked()-1);
    }

    public int showLikeCount(long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        int count = post.getLiked();
        return count;
    }
}
