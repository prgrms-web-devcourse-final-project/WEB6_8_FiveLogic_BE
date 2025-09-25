package com.back.domain.news.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.comment.repository.CommentRepository;
import com.back.domain.news.news.entity.News;
import com.back.domain.news.news.service.NewsService;
import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsService newsService;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        String content = "테스트 댓글입니다.";
        Comment comment = Comment.create(member, news, content);

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // when
        Comment createdComment = commentService.createComment(member, news, content);

        // then
        assertThat(createdComment).isNotNull();
        assertThat(createdComment.getContent()).isEqualTo(content);
        assertThat(createdComment.getMember()).isEqualTo(member);
        assertThat(createdComment.getNews()).isEqualTo(news);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("뉴스에 대한 댓글 목록 조회 성공")
    void getComments_Success() {
        // given
        News news = NewsFixture.createDefault();
        Comment comment1 = Comment.create(MemberFixture.createDefault(), news, "댓글1");
        Comment comment2 = Comment.create(MemberFixture.createDefault(), news, "댓글2");
        List<Comment> comments = Arrays.asList(comment1, comment2);

        when(commentRepository.findByNews(news)).thenReturn(comments);

        // when
        List<Comment> foundComments = commentService.getComments(news);

        // then
        assertThat(foundComments).hasSize(2);
        assertThat(foundComments).containsExactlyInAnyOrder(comment1, comment2);
        verify(commentRepository, times(1)).findByNews(news);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Comment existingComment = Comment.create(member, news, "기존 댓글");
        Long commentId = 1L;
        String updatedContent = "수정된 댓글입니다.";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when
        Comment result = commentService.updateComment(member, news, commentId, updatedContent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(updatedContent);
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외 발생")
    void updateComment_CommentNotFound_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long nonExistentCommentId = 999L;
        String updatedContent = "수정된 댓글입니다.";

        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            commentService.updateComment(member, news, nonExistentCommentId, updatedContent);
        });


        assertThat(exception.getMessage()).isEqualTo("Comment not found: " + nonExistentCommentId);
        verify(commentRepository, times(1)).findById(nonExistentCommentId);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 수정 시 예외 발생")
    void updateComment_NotAuthor_ThrowsException() {
        // given
        Member author = MemberFixture.createDefault();
        Member otherMember = MemberFixture.create(2L, "other@example.com", "other", "otherpass", Member.Role.MENTEE);
        News news = NewsFixture.createDefault();
        Comment existingComment = Comment.create(author, news, "기존 댓글");
        Long commentId = 1L;
        String updatedContent = "수정된 댓글입니다.";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when & then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            commentService.updateComment(otherMember, news, commentId, updatedContent);
        });

        assertThat(exception.getMessage()).isEqualTo("You do not have permission to update this comment.");
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("다른 뉴스의 댓글 수정 시 예외 발생")
    void updateComment_WrongNews_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news1 = NewsFixture.createDefault();
        News news2 = NewsFixture.create(2L, member, "다른 뉴스");
        Comment existingComment = Comment.create(member, news1, "기존 댓글");
        Long commentId = 1L;
        String updatedContent = "수정된 댓글입니다.";

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(member, news2, commentId, updatedContent);
        });

        assertThat(exception.getMessage()).isEqualTo("This comment does not belong to the given news.");
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Comment existingComment = Comment.create(member, news, "기존 댓글");
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        doNothing().when(commentRepository).delete(existingComment);

        // when
        commentService.deleteComment(member, news, commentId);

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(existingComment);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
    void deleteComment_CommentNotFound_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long nonExistentCommentId = 999L;

        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            commentService.deleteComment(member, news, nonExistentCommentId);
        });

        assertThat(exception.getMessage()).isEqualTo("Comment not found: " + nonExistentCommentId);
        verify(commentRepository, times(1)).findById(nonExistentCommentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 삭제 시 예외 발생")
    void deleteComment_NotAuthor_ThrowsException() {
        // given
        Member author = MemberFixture.createDefault();
        Member otherMember = MemberFixture.create(2L, "other@example.com", "other", "otherpass", Member.Role.MENTEE);
        News news = NewsFixture.createDefault();
        Comment existingComment = Comment.create(author, news, "기존 댓글");
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when & then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteComment(otherMember, news, commentId);
        });

        assertThat(exception.getMessage()).isEqualTo("You do not have permission to delete this comment.");
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("다른 뉴스의 댓글 삭제 시 예외 발생")
    void deleteComment_WrongNews_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news1 = NewsFixture.createDefault();
        News news2 = NewsFixture.create(2L, member, "다른 뉴스");
        Comment existingComment = Comment.create(member, news1, "기존 댓글");
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(member, news2, commentId);
        });

        assertThat(exception.getMessage()).isEqualTo("This comment does not belong to the given news.");
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
