package com.back.domain.post.comment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PostCommentTest {

    @Nested
    @DisplayName("PostComment 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("정상적인 PostComment 생성")
        void createComment_success() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);
            String content = "테스트 댓글";
            String role = member.getRole().name();

            // when
            PostComment comment = PostComment.builder()
                    .post(post)
                    .content(content)
                    .member(member)
                    .role(role)
                    .build();

            // then
            assertThat(comment.getPost()).isEqualTo(post);
            assertThat(comment.getContent()).isEqualTo(content);
            assertThat(comment.getMember()).isEqualTo(member);
            assertThat(comment.getRole()).isEqualTo(role);
            assertThat(comment.getIsAdopted()).isFalse();
        }

        @Test
        @DisplayName("댓글 생성 시 isAdopted는 기본적으로 false")
        void createComment_defaultIsAdoptedFalse() {
            // given
            Member member = MemberFixture.createDefault();
            Post post = createDefaultPost(member);

            // when
            PostComment comment = PostComment.builder()
                    .post(post)
                    .content("테스트 댓글")
                    .member(member)
                    .role(member.getRole().name())
                    .build();

            // then
            assertThat(comment.getIsAdopted()).isFalse();
        }
    }

    @Nested
    @DisplayName("댓글 내용 업데이트 테스트")
    class UpdateContentTest {

        @Test
        @DisplayName("정상적인 댓글 내용 업데이트")
        void updateContent_success() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);
            String newContent = "수정된 댓글 내용";

            // when
            comment.updateContent(newContent);

            // then
            assertThat(comment.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("null 내용으로 업데이트 시 예외 발생")
        void updateContent_withNull_throwsException() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);

            // when & then
            assertThatThrownBy(() -> comment.updateContent(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글을 입력해주세요");
        }

        @Test
        @DisplayName("빈 문자열로 업데이트 시 예외 발생")
        void updateContent_withEmpty_throwsException() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);

            // when & then
            assertThatThrownBy(() -> comment.updateContent(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글을 입력해주세요");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 업데이트 시 예외 발생")
        void updateContent_withWhitespace_throwsException() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);

            // when & then
            assertThatThrownBy(() -> comment.updateContent("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글을 입력해주세요");
        }
    }

    @Nested
    @DisplayName("게시글 업데이트 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("댓글의 게시글 업데이트 성공")
        void updatePost_success() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);
            Post newPost = createDefaultPost(member);

            // when
            comment.updatePost(newPost);

            // then
            assertThat(comment.getPost()).isEqualTo(newPost);
        }

        @Test
        @DisplayName("댓글의 게시글을 null로 업데이트")
        void updatePost_withNull() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);

            // when
            comment.updatePost(null);

            // then
            assertThat(comment.getPost()).isNull();
        }
    }

    @Nested
    @DisplayName("댓글 채택 테스트")
    class AdoptCommentTest {

        @Test
        @DisplayName("댓글 채택 성공")
        void adoptComment_success() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);

            // when
            comment.adoptComment();

            // then
            assertThat(comment.getIsAdopted()).isTrue();
        }

        @Test
        @DisplayName("이미 채택된 댓글도 채택 가능")
        void adoptComment_alreadyAdopted() {
            // given
            Member member = MemberFixture.createDefault();
            PostComment comment = createDefaultComment(member);
            comment.adoptComment(); // 먼저 채택

            // when
            comment.adoptComment(); // 다시 채택

            // then
            assertThat(comment.getIsAdopted()).isTrue();
        }
    }

    @Nested
    @DisplayName("작성자 확인 테스트")
    class AuthorCheckTest {

        @Test
        @DisplayName("댓글 작성자 확인 성공")
        void isAuthor_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            PostComment comment = createDefaultComment(author);

            // when
            Boolean isAuthor = comment.isAuthor(author);

            // then
            assertThat(isAuthor).isTrue();
        }

        @Test
        @DisplayName("다른 사용자는 댓글 작성자가 아님")
        void isAuthor_differentUser_false() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author", "password", Member.Role.MENTEE);
            Member otherUser = MemberFixture.create(2L, "other@test.com", "Other", "password", Member.Role.MENTEE);
            PostComment comment = createDefaultComment(author);

            // when
            Boolean isAuthor = comment.isAuthor(otherUser);

            // then
            assertThat(isAuthor).isFalse();
        }

        @Test
        @DisplayName("댓글 작성자 이름 반환")
        void getAuthorName_success() {
            // given
            Member author = MemberFixture.create(1L, "author@test.com", "Author Name", "password", Member.Role.MENTEE);
            PostComment comment = createDefaultComment(author);

            // when
            String authorName = comment.getAuthorName();

            // then
            assertThat(authorName).isEqualTo("Author Name");
        }
    }

    @Nested
    @DisplayName("역할(Role) 테스트")
    class RoleTest {

        @Test
        @DisplayName("멘티 역할로 댓글 생성")
        void createComment_withMenteeRole() {
            // given
            Member mentee = MemberFixture.create(1L, "mentee@test.com", "Mentee", "password", Member.Role.MENTEE);
            Post post = createDefaultPost(mentee);

            // when
            PostComment comment = PostComment.builder()
                    .post(post)
                    .content("멘티 댓글")
                    .member(mentee)
                    .role(mentee.getRole().name())
                    .build();

            // then
            assertThat(comment.getRole()).isEqualTo("MENTEE");
        }

        @Test
        @DisplayName("멘토 역할로 댓글 생성")
        void createComment_withMentorRole() {
            // given
            Member mentor = MemberFixture.create(1L, "mentor@test.com", "Mentor", "password", Member.Role.MENTOR);
            Post post = createDefaultPost(mentor);

            // when
            PostComment comment = PostComment.builder()
                    .post(post)
                    .content("멘토 댓글")
                    .member(mentor)
                    .role(mentor.getRole().name())
                    .build();

            // then
            assertThat(comment.getRole()).isEqualTo("MENTOR");
        }

        @Test
        @DisplayName("관리자 역할로 댓글 생성")
        void createComment_withAdminRole() {
            // given
            Member admin = MemberFixture.create(1L, "admin@test.com", "Admin", "password", Member.Role.ADMIN);
            Post post = createDefaultPost(admin);

            // when
            PostComment comment = PostComment.builder()
                    .post(post)
                    .content("관리자 댓글")
                    .member(admin)
                    .role(admin.getRole().name())
                    .build();

            // then
            assertThat(comment.getRole()).isEqualTo("ADMIN");
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

    private PostComment createDefaultComment(Member member) {
        Post post = createDefaultPost(member);
        return PostComment.builder()
                .post(post)
                .content("테스트 댓글")
                .member(member)
                .role(member.getRole().name())
                .build();
    }
}