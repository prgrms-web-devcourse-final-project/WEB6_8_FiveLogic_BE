package com.back.domain.news.news.entity;

import com.back.domain.file.video.entity.Video;
import com.back.domain.news.comment.entity.Comment;
import com.back.domain.news.like.entity.Like;
import com.back.fixture.MemberFixture;
import com.back.fixture.VideoFixture;
import com.back.domain.member.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsTest {
    @Test
    @DisplayName("제목과 영상,내용으로 뉴스 객체 생성")
    void newsCreationTestWithTitleAndContent() {
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        Member member = MemberFixture.createDefault();
        News news = News.create(member, title, video, content);

        assertThat(news).isNotNull();
        assertThat(news.getMember()).isEqualTo(member);
        assertThat(news.getTitle()).isEqualTo(title);
        assertThat(news.getVideo()).isEqualTo(video);
        assertThat(news.getContent()).isEqualTo(content);
        assertThat(news.getComment().size()).isEqualTo(0);
        assertThat(news.getLikes().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("멤버가 null인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidMember() {
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();

        try {
            News.create(null, title, video, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("제목이 null 혹은 공백인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidTitle() {
        Member member = MemberFixture.createDefault();
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();

        try {
            News.create(member, null, video, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            News.create(member, "", video, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("영상이 null인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidVideo() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";

        try {
            News.create(member, title, null, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("내용이 null 혹은 공백인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidContent() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        Video video = VideoFixture.createDefault();

        try {
            News.create(member, title, video, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            News.create(member, title, video, "");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("사용자는 뉴스에 좋아요를 누를 수 있다.")
    void likeNewsTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        Like like = Like.create(member, news);
        news.like(like);

        assertThat(news.getLikes().size()).isEqualTo(1);
        assertThat(news.getLikes()).contains(like);
    }

    @Test
    @DisplayName("사용자는 이미 좋아요를 누른 뉴스에 다시 좋아요를 누를 수 없다.")
    void likeNewsAlreadyLikedTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        Like like = Like.create(member, news);
        news.like(like);

        try {
            news.like(like);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
        }

        assertThat(news.getLikes().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("뉴스의 제목, 영상, 내용을 수정할 수 있다.")
    void updateNewsTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        String newTitle = "Updated News Title";
        String newContent = "This is the updated news content.";
        Video newVideo = VideoFixture.createDefault();

        news.update(newTitle, newVideo, newContent);

        assertThat(news.getTitle()).isEqualTo(newTitle);
        assertThat(news.getContent()).isEqualTo(newContent);
        assertThat(news.getVideo()).isEqualTo(newVideo);
    }

    @Test
    @DisplayName("업데이트시 제목이 null 혹은 공백인 경우 예외를 반환한다.")
    void updateNewsTestWithInvalidTitle() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        String newContent = "This is the updated news content.";
        Video newVideo = VideoFixture.createDefault();

        try {
            news.update(null, newVideo, newContent);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            news.update("", newVideo, newContent);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("업데이트시 영상이 null인 경우 예외를 반환한다.")
    void updateNewsTestWithInvalidVideo() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        String newTitle = "Updated News Title";
        String newContent = "This is the updated news content.";

        try {
            news.update(newTitle, null, newContent);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("업데이트시 내용이 null 혹은 공백인 경우 예외를 반환한다.")
    void updateNewsTestWithInvalidContent() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        String newTitle = "Updated News Title";
        Video newVideo = VideoFixture.createDefault();

        try {
            news.update(newTitle, newVideo, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            news.update(newTitle, newVideo, "");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("뉴스에 댓글을 추가할 수 있다.")
    void addCommentTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        Comment comment = Comment.create(member, news, "This is a comment.");
        news.addComment(comment);

        assertThat(news.getComment().size()).isEqualTo(1);
        assertThat(news.getComment()).contains(comment);
    }

    @Test
    @DisplayName("뉴스에 null 댓글을 추가하려 하면 예외를 반환한다.")
    void addNullCommentTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        try {
            news.addComment(null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        assertThat(news.getComment().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("뉴스의 댓글을 제거할 수 있다.")
    void removeCommentTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        Comment comment = Comment.create(member, news, "This is a comment.");
        news.addComment(comment);
        assertThat(news.getComment().size()).isEqualTo(1);

        news.removeComment(comment);
        assertThat(news.getComment().size()).isEqualTo(0);
    }
    @Test
    @DisplayName("뉴스의 null 댓글을 제거하려하면 예외를 반환한다.")
    void removeNullCommentTest() {
        Member member = MemberFixture.createDefault();
        String title = "Sample News Title";
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();
        News news = News.create(member, title, video, content);

        try {
            news.removeComment(null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        assertThat(news.getComment().size()).isEqualTo(0);
    }
}