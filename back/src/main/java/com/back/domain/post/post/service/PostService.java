package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.*;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Tag(name = "PostController", description = "커뮤니티(게시글) API")
public class PostService {

    private final PostRepository postRepository;;

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts;
    }

    @Transactional
    public PostCreateResponse createPost(PostCreateRequest postCreateRequest, Member member) {
        String postTypeStr = postCreateRequest.postType();
        Post.validPostType(postTypeStr);
        Post.PostType postType = Post.PostType.valueOf(postTypeStr);

        // PostType이 PracticePost인 경우 멘토인지 확인
        if (postType == Post.PostType.PRACTICEPOST && member.getRole() != Member.Role.MENTOR) {
            throw new ServiceException("400", "실무 경험 공유 게시글은 멘토만 작성할 수 있습니다.");
        }

        Post post = Post.builder()
                .title(postCreateRequest.title())
                .content(postCreateRequest.content())
                .member(member)
                .postType(postType)
                .build();


        if(postType == Post.PostType.PRACTICEPOST) {
            post.updateJob(postCreateRequest.job());
        }

        // PostType이 QUESTIONPOST인 경우 isResolve를 false로 초기화
        if(postType == Post.PostType.QUESTIONPOST) {
            post.updateResolveStatus(false);
        }

        postRepository.save(post);

        return PostCreateResponse.from(post);
    }


    @Transactional
    public void removePost(Long postId, Member member) {
        Post post = findById(postId);
        if (!post.isAuthor(member)) throw new ServiceException("400", "삭제 권한이 없습니다.");

        postRepository.delete(post);
    }

    @Transactional
    public void updatePost(long postId, Member member, @Valid PostModifyRequest postModifyRequest) {
        Post post = findById(postId);
        if (!post.isAuthor(member)) throw new ServiceException("400", "수정 권한이 없습니다.");

        if ( postModifyRequest.title() == null || postModifyRequest.title().isBlank()) {
            throw new ServiceException("400", "제목을 입력해주세요.");
        }

        if ( postModifyRequest.content() == null || postModifyRequest.content().isBlank()) {
            throw new ServiceException("400", "내용을 입력해주세요.");
        }

        post.updateTitle(postModifyRequest.title());
        post.updateContent(postModifyRequest.content());

        postRepository.save(post);
    }

    public Post getPostDetailWithViewIncrement(Long postId) {
        Post post = postRepository.findByIdWithMember(postId)
                .orElseThrow(()-> new ServiceException("400","해당 Id의 게시글이 없습니다."));
        post.increaseViewCount();

        return post;
    }

    public Page<PostDto> getPosts(String keyword, int page, int size ,Post.PostType postType) {
        Pageable pageable = PageRequest.of(page, size);

        return postRepository.searchPosts(keyword, pageable, postType).map(PostDto::from);
    }

    public Post findById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));
        return post;
    }

    @Transactional
    public PostSingleResponse makePostSingleResponse(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        PostSingleResponse postSingleResponse = PostSingleResponse.from(post);
        return postSingleResponse;
    }

    public List<PostAllResponse> getAllPostResponse() {
        return postRepository.findAllWithMember().stream()
                .map(PostAllResponse::from)
                .toList();
    }

    public boolean existsById(Long postId) {
        return postRepository.existsById(postId);
    }
}
