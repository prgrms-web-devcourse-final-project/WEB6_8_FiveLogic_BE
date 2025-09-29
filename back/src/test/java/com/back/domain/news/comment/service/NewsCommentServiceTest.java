package com.back.domain.news.comment.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.NewsComment;
import com.back.domain.news.comment.repository.NewsCommentRepository;
import com.back.domain.news.news.entity.News;
import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsCommentServiceTest {

    @Mock
    private NewsCommentRepository newsCommentRepository;

    @InjectMocks
    private NewsCommentService newsCommentService;

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        String content = "테스트 댓글입니다.";
        NewsComment newsComment = NewsComment.create(member, news, content);

        when(newsCommentRepository.save(any(NewsComment.class))).thenReturn(newsComment);

        // when
        NewsComment createdNewsComment = newsCommentService.createComment(member, news, content);

        // then
        assertThat(createdNewsComment).isNotNull();
        assertThat(createdNewsComment.getContent()).isEqualTo(content);
        assertThat(createdNewsComment.getMember()).isEqualTo(member);
        assertThat(createdNewsComment.getNews()).isEqualTo(news);
        verify(newsCommentRepository, times(1)).save(any(NewsComment.class));
    }

    @Test
    @DisplayName("뉴스에 대한 댓글 목록 조회 성공")
    void getComments_Success() {
        // given
        News news = NewsFixture.createDefault();
        NewsComment newsComment1 = NewsComment.create(MemberFixture.createDefault(), news, "댓글1");
        NewsComment newsComment2 = NewsComment.create(MemberFixture.createDefault(), news, "댓글2");
        List<NewsComment> newsComments = Arrays.asList(newsComment1, newsComment2);

        when(newsCommentRepository.findByNews(news)).thenReturn(newsComments);

        // when
        List<NewsComment> foundNewsComments = newsCommentService.getComments(news);

        // then
        assertThat(foundNewsComments).hasSize(2);
        assertThat(foundNewsComments).containsExactlyInAnyOrder(newsComment1, newsComment2);
        verify(newsCommentRepository, times(1)).findByNews(news);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        NewsComment existingNewsComment = NewsComment.create(member, news, "기존 댓글");
        Long commentId = 1L;
        String updatedContent = "수정된 댓글입니다.";

        when(newsCommentRepository.findById(commentId)).thenReturn(Optional.of(existingNewsComment));

        // when
        NewsComment result = newsCommentService.updateComment(member, news, commentId, updatedContent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(updatedContent);
        verify(newsCommentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외 발생")
    void updateComment_CommentNotFound_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long nonExistentCommentId = 999L;
        String updatedContent = "수정된 댓글입니다.";

        when(newsCommentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsCommentService.updateComment(member, news, nonExistentCommentId, updatedContent);
        });


        assertThat(exception.getMessage()).isEqualTo("404 : Comment not found: " + nonExistentCommentId);
        verify(newsCommentRepository, times(1)).findById(nonExistentCommentId);
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 수정 시 예외 발생")
    void updateComment_NotAuthor_ThrowsException() {
        // given
        Member author = MemberFixture.createDefault();
        Member otherMember = MemberFixture.create(2L, "other@example.com", "other", "otherpass", Member.Role.MENTEE);
        News news = NewsFixture.createDefault();
        NewsComment existingNewsComment = NewsComment.create(author, news, "기존 댓글");
        Long commentId = 1L;
        String updatedContent = "수정된 댓글입니다.";

        when(newsCommentRepository.findById(commentId)).thenReturn(Optional.of(existingNewsComment));

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsCommentService.updateComment(otherMember, news, commentId, updatedContent);
        });

        assertThat(exception.getMessage()).isEqualTo("403 : You do not have permission to update this comment.");
        verify(newsCommentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("다른 뉴스의 댓글 수정 시 예외 발생")
    void updateComment_WrongNews_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news1 = NewsFixture.createDefault();
        News news2 = NewsFixture.create(2L, member, "다른 뉴스");
        NewsComment existingNewsComment = NewsComment.create(member, news1, "기존 댓글");
        Long commentId = 1L;
        String updatedContent = "수정된 댓글입니다.";

        when(newsCommentRepository.findById(commentId)).thenReturn(Optional.of(existingNewsComment));

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsCommentService.updateComment(member, news2, commentId, updatedContent);
        });

        assertThat(exception.getMessage()).isEqualTo("400 : This comment does not belong to the given news.");
        verify(newsCommentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        NewsComment existingNewsComment = NewsComment.create(member, news, "기존 댓글");
        Long commentId = 1L;

        when(newsCommentRepository.findById(commentId)).thenReturn(Optional.of(existingNewsComment));
        doNothing().when(newsCommentRepository).delete(existingNewsComment);

        // when
        newsCommentService.deleteComment(member, news, commentId);

        // then
        verify(newsCommentRepository, times(1)).findById(commentId);
        verify(newsCommentRepository, times(1)).delete(existingNewsComment);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
    void deleteComment_CommentNotFound_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        Long nonExistentCommentId = 999L;

        when(newsCommentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsCommentService.deleteComment(member, news, nonExistentCommentId);
        });

        assertThat(exception.getMessage()).isEqualTo("404 : Comment not found: " + nonExistentCommentId);
        verify(newsCommentRepository, times(1)).findById(nonExistentCommentId);
        verify(newsCommentRepository, never()).delete(any(NewsComment.class));
    }

    @Test
    @DisplayName("댓글 작성자가 아닌 사용자가 삭제 시 예외 발생")
    void deleteComment_NotAuthor_ThrowsException() {
        // given
        Member author = MemberFixture.createDefault();
        Member otherMember = MemberFixture.create(2L, "other@example.com", "other", "otherpass", Member.Role.MENTEE);
        News news = NewsFixture.createDefault();
        NewsComment existingNewsComment = NewsComment.create(author, news, "기존 댓글");
        Long commentId = 1L;

        when(newsCommentRepository.findById(commentId)).thenReturn(Optional.of(existingNewsComment));

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsCommentService.deleteComment(otherMember, news, commentId);
        });

        assertThat(exception.getMessage()).isEqualTo("403 : You do not have permission to delete this comment.");
        verify(newsCommentRepository, times(1)).findById(commentId);
        verify(newsCommentRepository, never()).delete(any(NewsComment.class));
    }

    @Test
    @DisplayName("다른 뉴스의 댓글 삭제 시 예외 발생")
    void deleteComment_WrongNews_ThrowsException() {
        // given
        Member member = MemberFixture.createDefault();
        News news1 = NewsFixture.createDefault();
        News news2 = NewsFixture.create(2L, member, "다른 뉴스");
        NewsComment existingNewsComment = NewsComment.create(member, news1, "기존 댓글");
        Long commentId = 1L;

        when(newsCommentRepository.findById(commentId)).thenReturn(Optional.of(existingNewsComment));

        // when & then
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            newsCommentService.deleteComment(member, news2, commentId);
        });

        assertThat(exception.getMessage()).isEqualTo("400 : This comment does not belong to the given news.");
        verify(newsCommentRepository, times(1)).findById(commentId);
        verify(newsCommentRepository, never()).delete(any(NewsComment.class));
    }
}
