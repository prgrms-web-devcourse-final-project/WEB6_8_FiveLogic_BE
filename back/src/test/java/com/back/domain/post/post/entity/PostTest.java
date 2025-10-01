package com.back.domain.post.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.comment.entity.PostComment;
import com.back.fixture.MemberFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PostTest {

    @Nested
    @DisplayName("Post 생성 테스트")
    class CreatePostTest {

        @Test
        @DisplayName("정상적인 Post 생성")
        void createPost_success() {
            // given
            Member member = MemberFixture.createDefault();
            String title = "테스트 제목";
            String content = "테스트 내용";
            Post.PostType postType = Post.PostType.INFORMATIONPOST;

            // when
            Post post = Post.builder()
                    .title(title)
                    .content(content)
                    .member(member)
                    .postType(postType)
                    .build();

            // then
            assertThat(post.getTitle()).isEqualTo(title);
            assertThat(post.getContent()).isEqualTo(content);
            assertThat(post.getMember()).isEqualTo(member);
            assertThat(post.getPostType()).isEqualTo(postType);
            assertThat(post.getViewCount()).isEqualTo(0);
            assertThat(post.getComments()).isEmpty();
        }

        @Test
        @DisplayName("댓글 리스트가 null일 때 빈 리스트로 초기화")
        void createPost_withNullComments_initializeEmptyList() {
            // given
            Member member = MemberFixture.createDefault();

            // when
            Post post = Post.builder()
                    .title("제목")
                    .content("내용")
                    .member(member)
                    .postType(Post.PostType.INFORMATIONPOST)
                    .comments(null)
                    .build();

            // then
            assertThat(post.getComments()).isNotNull();
            assertThat(post.getComments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("PostType 검증 테스트")
    class ValidPostTypeTest {

        @Test
        @DisplayName("유효한 PostType 검증 성공")
        void validPostType_success() {
            // given & when & then
            assertThatNoException().isThrownBy(() -> Post.validPostType("INFORMATIONPOST"));
            assertThatNoException().isThrownBy(() -> Post.validPostType("PRACTICEPOST"));
            assertThatNoException().isThrownBy(() -> Post.validPostType("QUESTIONPOST"));
        }

        @Test
        @DisplayName("유효하지 않은 PostType 검증 실패")
        void validPostType_failure() {
            // given
            String invalidPostType = "INVALIDPOST";

            // when & then
            assertThatThrownBy(() -> Post.validPostType(invalidPostType))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 유효하지 않은 PostType입니다.");
        }
    }

    @Nested
    @DisplayName("게시글 업데이트 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("제목 업데이트 성공")
        void updateTitle_success() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);
            String newTitle = "새로운 제목";

            // when
            post.updateTitle(newTitle);

            // then
            assertThat(post.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("null 제목으로 업데이트 실패")
        void updateTitle_withNull_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when & then
            assertThatThrownBy(() -> post.updateTitle(null))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 제목은 null이거나 공백일 수 없습니다.");
        }

        @Test
        @DisplayName("공백 제목으로 업데이트 실패")
        void updateTitle_withBlank_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when & then
            assertThatThrownBy(() -> post.updateTitle("   "))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 제목은 null이거나 공백일 수 없습니다.");
        }

        @Test
        @DisplayName("내용 업데이트 성공")
        void updateContent_success() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);
            String newContent = "새로운 내용";

            // when
            post.updateContent(newContent);

            // then
            assertThat(post.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("null 내용으로 업데이트 실패")
        void updateContent_withNull_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when & then
            assertThatThrownBy(() -> post.updateContent(null))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 내용은 null이거나 공백일 수 없습니다.");
        }

        @Test
        @DisplayName("공백 내용으로 업데이트 실패")
        void updateContent_withBlank_failure() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when & then
            assertThatThrownBy(() -> post.updateContent("   "))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("400 : 내용은 null이거나 공백일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("댓글 관리 테스트")
    class CommentManagementTest {

        @Test
        @DisplayName("댓글 추가 성공")
        void addComment_success() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);
            PostComment comment = createComment(member, post);

            // when
            post.addComment(comment);

            // then
            assertThat(post.getComments()).hasSize(1);
            assertThat(post.getComments()).contains(comment);
            assertThat(comment.getPost()).isEqualTo(post);
        }

        @Test
        @DisplayName("댓글 제거 성공")
        void removeComment_success() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);
            PostComment comment = createComment(member, post);
            post.addComment(comment);

            // when
            post.removeComment(comment);

            // then
            assertThat(post.getComments()).isEmpty();
            assertThat(comment.getPost()).isNull();
        }
    }

    @Nested
    @DisplayName("작성자 확인 테스트")
    class AuthorCheckTest {

        @Test
        @DisplayName("작성자 확인 성공")
        void isAuthor_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);

            // when
            Boolean isAuthor = post.isAuthor(author);

            // then
            assertThat(isAuthor).isTrue();
        }

        @Test
        @DisplayName("다른 사용자는 작성자가 아님")
        void isAuthor_differentUser_false() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member otherUser = MemberFixture.create(2L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);

            // when
            Boolean isAuthor = post.isAuthor(otherUser);

            // then
            assertThat(isAuthor).isFalse();
        }

        @Test
        @DisplayName("작성자 이름 반환")
        void getAuthorName_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author Name", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(author);

            // when
            String authorName = post.getAuthorName();

            // then
            assertThat(authorName).isEqualTo("Test Nickname");
        }
    }

    @Nested
    @DisplayName("조회수 증가 테스트")
    class ViewCountTest {

        @Test
        @DisplayName("조회수 증가 성공")
        void increaseViewCount_success() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);
            int initialViewCount = post.getViewCount();

            // when
            post.increaseViewCount();

            // then
            assertThat(post.getViewCount()).isEqualTo(initialViewCount + 1);
        }

        @Test
        @DisplayName("조회수 여러 번 증가")
        void increaseViewCount_multiple_times() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when
            post.increaseViewCount();
            post.increaseViewCount();
            post.increaseViewCount();

            // then
            assertThat(post.getViewCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("해결 상태 업데이트 테스트")
    class ResolveStatusTest {

        @Test
        @DisplayName("해결 상태를 true로 업데이트")
        void updateResolveStatus_true() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when
            post.updateResolveStatus(true);

            // then
            assertThat(post.getIsResolve()).isTrue();
        }

        @Test
        @DisplayName("해결 상태를 false로 업데이트")
        void updateResolveStatus_false() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when
            post.updateResolveStatus(false);

            // then
            assertThat(post.getIsResolve()).isFalse();
        }
    }

    private Post createDefaultPost(Member member) {
        return Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .member(member)
                .postType(Post.PostType.INFORMATIONPOST)
                .build();
    }

    private PostComment createComment(Member member, Post post) {
        return PostComment.builder()
                .post(post)
                .content("테스트 댓글")
                .member(member)
                .role(member.getRole().name())
                .build();
    }
}
