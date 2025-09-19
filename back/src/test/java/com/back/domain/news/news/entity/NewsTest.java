package com.back.domain.news.news.entity;

import com.back.domain.file.entity.Video;
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
        assertThat(news.getLikes()).isEqualTo(0);
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
}