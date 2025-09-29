package com.back.domain.news.comment.entity;


import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsCommentTest {
    @Test
    @DisplayName("사용자, 뉴스, 내용으로 댓글 객체 생성")
    void commentCreationTest() {
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        String content = "This is a sample comment content.";
        NewsComment newsComment = NewsComment.create(member, news, content);

        assertThat(newsComment).isNotNull();
        assertThat(newsComment.getMember()).isEqualTo(member);
        assertThat(newsComment.getNews()).isEqualTo(news);
        assertThat(newsComment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("멤버가 null인 경우 예외를 반환한다.")
    void commentCreationTestWithInvalidMember() {
        News news = NewsFixture.createDefault();
        String content = "This is a sample comment content.";

        try {
            NewsComment.create(null, news, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("뉴스가 null인 경우 예외를 반환한다.")
    void commentCreationTestWithInvalidNews() {
        Member member = MemberFixture.createDefault();
        String content = "This is a sample comment content.";

        try {
            NewsComment.create(member, null, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("내용이 null 혹은 공백인 경우 예외를 반환한다.")
    void commentCreationTestWithInvalidContent() {
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();

        try {
            NewsComment.create(member, news, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            NewsComment.create(member, news, "");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("댓글 내용을 수정할 수 있다.")
    void commentUpdateTest() {
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        String content = "This is a sample comment content.";
        NewsComment newsComment = NewsComment.create(member, news, content);

        String newContent = "This is the updated comment content.";
        newsComment.update(newContent);

        assertThat(newsComment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("댓글 수정 시 내용이 null 혹은 공백인 경우 예외를 반환한다.")
    void commentUpdateTestWithInvalidContent() {
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();
        String content = "This is a sample comment content.";
        NewsComment newsComment = NewsComment.create(member, news, content);

        try {
            newsComment.update(null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            newsComment.update("");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
