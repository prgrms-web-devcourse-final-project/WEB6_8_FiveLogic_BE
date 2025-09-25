package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.like.entity.PostLike;
import com.back.domain.post.like.repository.PostLikeRepository;
import com.back.domain.post.post.dto.PostAllResponse;
import com.back.domain.post.post.dto.PostCreateRequest;
import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final Rq rq;

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts;
    }

    @Transactional
    public Post createPost(PostCreateRequest postCreateRequest, Member member) {
        String postTypeStr = postCreateRequest.getPostType();
        Post.validPostType(postTypeStr);
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

    @Transactional
    public void removePost(Long postId, Member member) {
        Post post = findById(postId);
        if (!post.checkAuthority(post, member)) throw new ServiceException("400", "삭제 권한이 없습니다.");

        postRepository.delete(post);
    }

    @Transactional
    public void updatePost(long postId, Member member, @Valid PostCreateRequest postCreateRequest) {
        Post post = findById(postId);
        if (!post.checkAuthority(post, member)) throw new ServiceException("400", "수정 권한이 없습니다.");

        post.setTitle(postCreateRequest.getTitle());
        post.setContent(postCreateRequest.getContent());

        postRepository.save(post);
    }


    @Transactional()
    public int showLikeCount(long postId) {
        Post post = findById(postId);

        int count = post.getLiked();
        return count;
    }


    public Page<PostDto> getPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return postRepository.searchPosts(keyword, pageable).map(PostDto::from);
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
}
