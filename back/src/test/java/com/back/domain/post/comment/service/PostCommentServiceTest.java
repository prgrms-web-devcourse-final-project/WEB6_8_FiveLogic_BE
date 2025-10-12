package com.back.domain.post.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.dto.CommentAllResponse;
import com.back.domain.post.comment.dto.CommentCreateRequest;
import com.back.domain.post.comment.dto.CommentDeleteRequest;
import com.back.domain.post.comment.dto.CommentModifyRequest;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.repository.PostCommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.fixture.MemberFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @InjectMocks
    private PostCommentService postCommentService;

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("댓글 생성 성공")
        void createComment_success() {
            // given
            Member member = MemberFixture.create(1L, "user@test.com", "User", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(member);
            Long postId = 1L;
            CommentCreateRequest request = new CommentCreateRequest("테스트 댓글");

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));
            when(postCommentRepository.save(any(PostComment.class))).thenReturn(any(PostComment.class));

            // when
            postCommentService.createComment(member, postId, request);

            // then
            verify(postRepository).findById(postId);
            verify(postCommentRepository).save(any(PostComment.class));
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 생성 시 실패")
        void createComment_postNotExists_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Long postId = 999L;
            CommentCreateRequest request = new CommentCreateRequest("테스트 댓글");

            when(postRepository.findById(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postCommentService.createComment(member, postId, request))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 게시글이 없습니다.");

            verify(postCommentRepository, never()).save(any(PostComment.class));
        }
    }

    @Nested
    @DisplayName("댓글 조회 테스트")
    class GetCommentTest {

        @Test
        @DisplayName("게시글의 모든 댓글 조회 성공")
        void getAllPostCommentResponse_success() {
            // given
            Member member1 = MemberFixture.create(1L, "user1@test.com", "User1", "password", Member.Role.MENTEE);
            Member member2 = MemberFixture.create(2L, "user2@test.com", "User2", "password", Member.Role.MENTOR);
            Post post = createDefaultPost(member1);
            Long postId = 1L;

            List<PostComment> comments = Arrays.asList(
                    createComment(member1, post, "첫 번째 댓글"),
                    createComment(member2, post, "두 번째 댓글")
            );

            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findCommentsWithMemberByPostId(postId)).thenReturn(comments);

            // when
            List<CommentAllResponse> result = postCommentService.getAllPostCommentResponse(postId);

            // then
            assertThat(result).hasSize(2);
            verify(postRepository).existsById(postId);
            verify(postCommentRepository).findCommentsWithMemberByPostId(postId);
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 댓글 조회 시 실패")
        void getAllPostCommentResponse_postNotExists_failure() {
            // given
            Long postId = 999L;

            when(postRepository.existsById(postId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> postCommentService.getAllPostCommentResponse(postId))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 게시글이 없습니다.");

            verify(postCommentRepository, never()).findCommentsWithMemberByPostId(anyLong());
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class RemoveCommentTest {

        @Test
        @DisplayName("댓글 작성자가 댓글 삭제 성공")
        void removePostComment_author_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);
            PostComment comment = createComment(author, post, "삭제할 댓글");
            Long postId = 1L;
            Long commentId = 1L;
            CommentDeleteRequest request = new CommentDeleteRequest(commentId);


            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            postCommentService.removePostComment(postId, request, author);

            // then
            verify(postCommentRepository).delete(comment);
        }

        @Test
        @DisplayName("댓글 작성자가 아닌 사용자가 댓글 삭제 시도 시 실패")
        void removePostComment_notAuthor_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member otherUser = MemberFixture.create(2L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);
            PostComment comment = createComment(author, post, "삭제할 댓글");
            Long postId = 1L;
            Long commentId = 1L;
            CommentDeleteRequest request = new CommentDeleteRequest(commentId);


            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.removePostComment(postId, request, otherUser))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 삭제 권한이 없습니다.");

            verify(postCommentRepository, never()).delete(any(PostComment.class));
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시도 시 실패")
        void removePostComment_commentNotExists_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Long postId = 1L;
            Long commentId = 999L;
            CommentDeleteRequest request = new CommentDeleteRequest(commentId);

            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postCommentService.removePostComment(postId, request, member))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 댓글이 없습니다.");

            verify(postCommentRepository, never()).delete(any(PostComment.class));
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class UpdateCommentTest {

        @Test
        @DisplayName("댓글 작성자가 댓글 수정 성공")
        void updatePostComment_author_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);
            PostComment comment = createComment(author, post, "원본 댓글");
            Long postId = 1L;
            Long commentId = 1L;
            CommentModifyRequest request = new CommentModifyRequest(commentId,"수정된 댓글");

            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when
            postCommentService.updatePostComment(postId, request, author);

            // then
            verify(postCommentRepository).findById(commentId);
        }

        @Test
        @DisplayName("댓글 작성자가 아닌 사용자가 댓글 수정 시도 시 실패")
        void updatePostComment_notAuthor_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member otherUser = MemberFixture.create(2L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);
            PostComment comment = createComment(author, post, "원본 댓글");
            Long postId = 1L;
            Long commentId = 1L;

            CommentModifyRequest request = new CommentModifyRequest(commentId, "400 : 수정된 댓글");

            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.updatePostComment(postId, request, otherUser))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 수정 권한이 없습니다.");
        }

        @Test
        @DisplayName("빈 내용으로 댓글 수정 시도 시 실패")
        void updatePostComment_emptyContent_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);
            PostComment comment = createComment(author, post, "원본 댓글");
            Long postId = 1L;
            Long commentId = 1L;

            CommentModifyRequest request = new CommentModifyRequest(commentId, "");


            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.updatePostComment(postId, request, author))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 댓글은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("null 내용으로 댓글 수정 시도 시 실패")
        void updatePostComment_nullContent_failure() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);
            PostComment comment = createComment(author, post, "원본 댓글");
            Long postId = 1L;
            Long commentId = 1L;
            CommentModifyRequest request = new CommentModifyRequest(commentId, null);

            when(postRepository.existsById(postId)).thenReturn(true);
            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.updatePostComment(postId, request, author))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 댓글은 비어 있을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("댓글 채택 테스트")
    class AdoptCommentTest {

        @Test
        @DisplayName("질문 게시글 작성자가 댓글 채택 성공")
        void adoptComment_questionPostAuthor_success() {
            // given
            Member postAuthor = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member commenter = MemberFixture.create(2L, "commenter@test.com", "Commenter", "password", Member.Role.MENTOR);
            Post questionPost = createQuestionPost(postAuthor);
            PostComment comment = createComment(commenter, questionPost, "답변 댓글");
            Long commentId = 1L;

            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(postCommentRepository.existsByPostAndIsAdoptedTrue(questionPost)).thenReturn(false);

            // when
            postCommentService.adoptComment(commentId, postAuthor);

            // then
            verify(postCommentRepository).findById(commentId);
            verify(postCommentRepository).existsByPostAndIsAdoptedTrue(questionPost);
        }

        @Test
        @DisplayName("게시글 작성자가 아닌 사용자가 댓글 채택 시도 시 실패")
        void adoptComment_notPostAuthor_failure() {
            // given
            Member postAuthor = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member commenter = MemberFixture.create(2L, "commenter@test.com", "Commenter", "password", Member.Role.MENTOR);
            Member otherUser = MemberFixture.create(3L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            Post questionPost = createQuestionPost(postAuthor);
            PostComment comment = createComment(commenter, questionPost, "답변 댓글");
            Long commentId = 1L;

            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.adoptComment(commentId, otherUser))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 채택 권한이 없습니다.");
        }

        @Test
        @DisplayName("질문 게시글이 아닌 게시글의 댓글 채택 시도 시 실패")
        void adoptComment_notQuestionPost_failure() {
            // given
            Member postAuthor = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member commenter = MemberFixture.create(2L, "commenter@test.com", "Commenter", "password", Member.Role.MENTOR);
            Post informationPost = createDefaultPost(postAuthor);
            PostComment comment = createComment(commenter, informationPost, "일반 댓글");
            Long commentId = 1L;

            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.adoptComment(commentId, postAuthor))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 질문 게시글에만 댓글 채택이 가능합니다.");
        }

        @Test
        @DisplayName("이미 채택된 댓글을 다시 채택 시도 시 실패")
        void adoptComment_alreadyAdopted_failure() {
            // given
            Member postAuthor = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member commenter = MemberFixture.create(2L, "commenter@test.com", "Commenter", "password", Member.Role.MENTOR);
            Post questionPost = createQuestionPost(postAuthor);
            PostComment comment = createComment(commenter, questionPost, "답변 댓글");
            comment.adoptComment(); // 이미 채택된 상태
            Long commentId = 1L;

            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> postCommentService.adoptComment(commentId, postAuthor))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 이미 채택된 댓글입니다.");
        }

        @Test
        @DisplayName("이미 다른 댓글이 채택된 게시글에서 댓글 채택 시도 시 실패")
        void adoptComment_anotherCommentAlreadyAdopted_failure() {
            // given
            Member postAuthor = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member commenter = MemberFixture.create(2L, "commenter@test.com", "Commenter", "password", Member.Role.MENTOR);
            Post questionPost = createQuestionPost(postAuthor);
            PostComment comment = createComment(commenter, questionPost, "답변 댓글");
            Long commentId = 1L;

            when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(postCommentRepository.existsByPostAndIsAdoptedTrue(questionPost)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> postCommentService.adoptComment(commentId, postAuthor))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 이미 채택된 댓글이 있습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 채택 시도 시 실패")
        void adoptComment_commentNotExists_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Long commentId = 999L;

            when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postCommentService.adoptComment(commentId, member))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 해당 Id의 댓글이 없습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 존재 검증 테스트")
    class ValidatePostExistsTest {

        @Test
        @DisplayName("null 게시글 ID 검증 실패")
        void validatePostExists_nullId_failure() {
            // given
            Long postId = null;

            // when & then
            assertThatThrownBy(() -> postCommentService.getAllPostCommentResponse(postId))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 유효하지 않은 게시글 Id입니다.");
        }

        @Test
        @DisplayName("0 이하의 게시글 ID 검증 실패")
        void validatePostExists_invalidId_failure() {
            // given
            Long postId = 0L;

            // when & then
            assertThatThrownBy(() -> postCommentService.getAllPostCommentResponse(postId))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 유효하지 않은 게시글 Id입니다.");
        }
    }

    private Post createDefaultPost(Member member) {
        return Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .member(member)
                .postType(Post.PostType.INFORMATIONPOST)
                .build();
    }

    private Post createQuestionPost(Member member) {
        return Post.builder()
                .title("질문 게시글")
                .content("질문 내용")
                .member(member)
                .postType(Post.PostType.QUESTIONPOST)
                .build();
    }

    private PostComment createComment(Member member, Post post, String content) {
        return PostComment.builder()
                .post(post)
                .content(content)
                .member(member)
                .role(member.getRole().name())
                .build();
    }
}
