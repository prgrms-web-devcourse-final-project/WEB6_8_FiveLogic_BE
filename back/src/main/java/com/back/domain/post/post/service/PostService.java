package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.PostAllResponse;
import com.back.domain.post.post.dto.PostCreateRequest;
import com.back.domain.post.post.dto.PostDto;
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
    public Post createPost(PostCreateRequest postCreateRequest, Member member) {
        String postTypeStr = postCreateRequest.getPostType();
        Post.validPostType(postTypeStr);
        Post.PostType postType = Post.PostType.valueOf(postTypeStr);

        // PostType이 PracticePost인 경우 멘토인지 확인
        if (postType == Post.PostType.PRACTICEPOST && member.getRole() != Member.Role.MENTOR) {
            throw new ServiceException("400", "실무 경험 공유 게시글은 멘토만 작성할 수 있습니다.");
        }

//        if( postType == Post.PostType.PRACTICEPOST ) {
//            if(member.getCareer() == null || member.getCareer().isEmpty()) {
//                throw new ServiceException("400", "멘토는 경력을 입력해야 실무 경험 공유 게시글을 작성할 수 있습니다.");
//            }
//        }

        Post post = Post.builder()
                .title(postCreateRequest.getTitle())
                .content(postCreateRequest.getContent())
                .member(member)
                .postType(postType)
                .build();


        // PostType이 QUESTIONPOST인 경우 isResolve를 false로 초기화
        if(postType == Post.PostType.QUESTIONPOST) {
            post.updateResolveStatus(false);
        }

        postRepository.save(post);

        return post;
    }

    @Transactional
    public void removePost(Long postId, Member member) {
        Post post = findById(postId);
        if (!post.isAuthor(member)) throw new ServiceException("400", "삭제 권한이 없습니다.");

        postRepository.delete(post);
    }

    @Transactional
    public void updatePost(long postId, Member member, @Valid PostCreateRequest postCreateRequest) {
        Post post = findById(postId);
        if (!post.isAuthor(member)) throw new ServiceException("400", "수정 권한이 없습니다.");

        if ( postCreateRequest.getTitle() == null || postCreateRequest.getTitle().isBlank()) {
            throw new ServiceException("400", "제목을 입력해주세요.");
        }

        if ( postCreateRequest.getContent() == null || postCreateRequest.getContent().isBlank()) {
            throw new ServiceException("400", "내용을 입력해주세요.");
        }

        post.updateTitle(postCreateRequest.getTitle());
        post.updateContent(postCreateRequest.getContent());

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

    public List<PostAllResponse> getAllPostResponse() {
        return postRepository.findAllWithMember().stream()
                .map(PostAllResponse::new)
                .toList();
    }

    //채택된 comment 받아오기

}
