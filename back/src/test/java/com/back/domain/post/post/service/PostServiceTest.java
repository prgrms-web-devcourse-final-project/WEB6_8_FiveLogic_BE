package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.PostCreateRequest;
import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.dto.PostModifyRequest;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.fixture.MemberFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePostTest {

        @Test
        @DisplayName("정보 공유 게시글 생성 성공")
        void createPost_informationPost_success() {
            // given
            Member member = MemberFixture.create(1L, "test@test.com", "Test User", "password", Member.Role.MENTEE);
            PostCreateRequest request = new PostCreateRequest("INFORMATIONPOST","제목","내용","");

            // when
            postService.createPost(request, member);

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(captor.capture());

            Post savedPost = captor.getValue();

            // then
            assertThat(savedPost.getPostType()).isEqualTo(Post.PostType.INFORMATIONPOST);
            assertThat(savedPost.getTitle()).isEqualTo("제목");
            assertThat(savedPost.getContent()).isEqualTo("내용");
            assertThat(savedPost.getJob()).isNull();
            assertThat(savedPost.getMember()).isEqualTo(member);
        }

        @Test
        @DisplayName("실무 경험 공유 게시글 생성 성공 - 멘토")
        void createPost_practicePost_mentor_success() {
            // given
            Member mentor = MemberFixture.create(1L, "mentor@test.com", "Mentor", "password", Member.Role.MENTOR);
            PostCreateRequest request = new PostCreateRequest("PRACTICEPOST","실무경험","실무내용","백엔드");

            // when
            postService.createPost(request, mentor);


            // then
            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(captor.capture());

            Post savedPost = captor.getValue();

            assertThat(savedPost.getPostType()).isEqualTo(Post.PostType.PRACTICEPOST);
            assertThat(savedPost.getTitle()).isEqualTo("실무경험");
            assertThat(savedPost.getContent()).isEqualTo("실무내용");
            assertThat(savedPost.getJob()).isEqualTo("백엔드");
            assertThat(savedPost.getMember()).isEqualTo(mentor);
        }

        @Test
        @DisplayName("실무 경험 공유 게시글 생성 실패 - 멘티")
        void createPost_practicePost_mentee_failure() {
            // given
            Member mentee = MemberFixture.create(1L, "mentee@test.com", "Mentee", "password", Member.Role.MENTEE);
            PostCreateRequest request = new PostCreateRequest("PRACTICEPOST","실무경험","실무내용","");

            // when & then
            assertThatThrownBy(() -> postService.createPost(request, mentee))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 실무 경험 공유 게시글은 멘토만 작성할 수 있습니다.");

            verify(postRepository, never()).save(any(Post.class));
        }

        @Test
        @DisplayName("질문 게시글 생성 시 isResolve false로 초기화")
        void createPost_questionPost_initializeIsResolve() {
            // given
            Member member = MemberFixture.create(1L, "test@test.com", "Test User", "password", Member.Role.MENTEE);
            PostCreateRequest request = new PostCreateRequest("QUESTIONPOST","질문경험","질문내용","");


            // when
            postService.createPost(request, member);

            // then

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(captor.capture());

            Post savedPost = captor.getValue();

            assertThat(savedPost.getPostType()).isEqualTo(Post.PostType.QUESTIONPOST);
            assertThat(savedPost.getTitle()).isEqualTo("질문경험");
            assertThat(savedPost.getContent()).isEqualTo("질문내용");
            assertThat(savedPost.getMember()).isEqualTo(member);
            assertThat(savedPost.getIsResolve()).isFalse();



        }

        @Test
        @DisplayName("유효하지 않은 PostType으로 게시글 생성 실패")
        void createPost_invalidPostType_failure() {
            // given
            Member member = MemberFixture.createDefault();
            PostCreateRequest request = new PostCreateRequest("INVALIDPOST","질문경험","질문내용","");

            // when & then
            assertThatThrownBy(() -> postService.createPost(request, member))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 유효하지 않은 PostType입니다.");

            verify(postRepository, never()).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("게시글 삭제 테스트")
    class RemovePostTest {

        @Test
        @DisplayName("작성자가 게시글 삭제 성공")
        void removePost_author_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", author, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when
            postService.removePost(postId, author);

            // then
            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 게시글 삭제 시도 시 실패")
        void removePost_notAuthor_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member otherUser = MemberFixture.create(2L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", author, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.removePost(postId, otherUser))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 변경 권한이 없습니다.");

            verify(postRepository, never()).delete(any(Post.class));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시도 시 실패")
        void removePost_notExists_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Long postId = 999L;

            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.removePost(postId, member))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 게시글이 없습니다.");

            verify(postRepository, never()).delete(any(Post.class));
        }
    }

    @Nested
    @DisplayName("게시글 수정 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("작성자가 게시글 수정 성공")
        void updatePost_author_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createPost("기존 제목", "기존 내용", author, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;
            PostModifyRequest updateRequest = new PostModifyRequest("새 제목","새 내용");

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when
            postService.updatePost(postId, author, updateRequest);

            // then
            verify(postRepository).save(post);
            assertThat(post.getTitle()).isEqualTo("새 제목");
            assertThat(post.getContent()).isEqualTo("새 내용");
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 게시글 수정 시도 시 실패")
        void updatePost_notAuthor_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member otherUser = MemberFixture.create(2L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", author, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;
            PostModifyRequest updateRequest = new PostModifyRequest("새 내용","새 제목");


            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(postId, otherUser, updateRequest))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 변경 권한이 없습니다.");

            verify(postRepository, never()).save(any(Post.class));
        }

        @Test
        @DisplayName("제목이 null이거나 공백일 때 수정 실패")
        void updatePost_nullOrBlankTitle_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", author, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;
            PostModifyRequest updateRequest = new PostModifyRequest("","새 내용");

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(postId, author, updateRequest))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 제목을 입력해주세요.");

            verify(postRepository, never()).save(any(Post.class));
        }

        @Test
        @DisplayName("내용이 null이거나 공백일 때 수정 실패")
        void updatePost_nullOrBlankContent_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", author, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;
            PostModifyRequest updateRequest = new PostModifyRequest("새 제목","");

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(postId, author, updateRequest))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 내용을 입력해주세요.");

            verify(postRepository, never()).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("게시글 조회 테스트")
    class GetPostTest {

        @Test
        @DisplayName("게시글 다건 조회 성공")
        void getAllPosts_success() {
            // given
            Member member1 = MemberFixture.create(1L, "user1@test.com", "User1", "password", Member.Role.MENTEE);
            Member member2 = MemberFixture.create(2L, "user2@test.com", "User2", "password", Member.Role.MENTOR);

            List<Post> posts = Arrays.asList(
                    createPost("제목1", "내용1", member1, Post.PostType.INFORMATIONPOST),
                    createPost("제목2", "내용2", member2, Post.PostType.PRACTICEPOST)
            );

            when(postRepository.findAll()).thenReturn(posts);

            // when
            List<Post> result = postService.getAllPosts();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(posts);
            verify(postRepository).findAll();
        }

        @Test
        @DisplayName("게시글 상세 조회 시 조회수 증가")
        void getPostDetailWithViewIncrement_success() {
            // given
            Member member = MemberFixture.create(1L, "user@test.com", "User", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", member, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;
            int initialViewCount = post.getViewCount();

            when(postRepository.findByIdWithMember(postId)).thenReturn(Optional.of(post));

            // when
            Post result = postService.getPostDetailWithViewIncrement(postId);

            // then
            assertThat(result.getViewCount()).isEqualTo(initialViewCount + 1);
            verify(postRepository).findByIdWithMember(postId);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 상세 조회 시 실패")
        void getPostDetailWithViewIncrement_notExists_failure() {
            // given
            Long postId = 999L;

            when(postRepository.findByIdWithMember(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPostDetailWithViewIncrement(postId))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 게시글이 없습니다.");
        }

        @Test
        @DisplayName("페이징으로 게시글 검색 성공")
        void getPosts_withPaging_success() {
            // given
            String keyword = "테스트";
            int page = 0;
            int size = 10;
            Post.PostType postType = Post.PostType.INFORMATIONPOST;

            Member member = MemberFixture.create(1L, "user@test.com", "User", "password", Member.Role.MENTEE);

            List<Post> posts = Arrays.asList(
                    createPost("테스트 제목1", "테스트 내용1", member, Post.PostType.INFORMATIONPOST)
            );

            Pageable expectedPageable = PageRequest.of(page, size);
            Page<Post> postPage = new PageImpl<>(posts, expectedPageable, 1);

            when(postRepository.searchPosts(anyString(), any(Pageable.class), any(Post.PostType.class)))
                    .thenReturn(postPage);

            // when
            Page<PostDto> result = postService.getPosts(keyword, page, size, postType);

            // then - ArgumentCaptor로 인자 캡처
            ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            ArgumentCaptor<Post.PostType> postTypeCaptor = ArgumentCaptor.forClass(Post.PostType.class);

            verify(postRepository).searchPosts(
                    keywordCaptor.capture(),
                    pageableCaptor.capture(),
                    postTypeCaptor.capture()
            );

            // 전달된 인자 검증
            assertThat(keywordCaptor.getValue()).isEqualTo("테스트");
            assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
            assertThat(postTypeCaptor.getValue()).isEqualTo(Post.PostType.INFORMATIONPOST);

            // 결과 검증
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("ID로 게시글 찾기 성공")
        void findById_success() {
            // given
            Member member = MemberFixture.create(1L, "user@test.com", "User", "password", Member.Role.MENTEE);
            Post post = createPost("제목", "내용", member, Post.PostType.INFORMATIONPOST);
            Long postId = 1L;

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));

            // when
            Post result = postService.findPostById(postId);

            // then
            assertThat(result).isEqualTo(post);
            verify(postRepository).findById(postId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 게시글 찾기 실패")
        void findById_notExists_failure() {
            // given
            Long postId = 999L;

            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.findPostById(postId))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 게시글이 없습니다.");
        }
    }

    private Post createPost(String title, String content, Member member, Post.PostType postType) {
        return Post.builder()
                .title(title)
                .content(content)
                .member(member)
                .postType(postType)
                .build();
    }
}
