package com.back.domain.news.news.entity;

import com.back.domain.file.entity.Video;
import com.back.domain.fixture.VideoFixture;
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

        News news = News.create(title, video, content);

        assertThat(news).isNotNull();
        assertThat(news.getTitle()).isEqualTo(title);
        assertThat(news.getVideo()).isEqualTo(video);
        assertThat(news.getContent()).isEqualTo(content);
        assertThat(news.getLikes()).isEqualTo(0);
    }

    @Test
    @DisplayName("제목이 null 혹은 공백인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidTitle() {
        String content = "This is a sample news content.";
        Video video = VideoFixture.createDefault();

        try {
            News.create(null, video, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            News.create("", video, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("영상이 null인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidVideo() {
        String title = "Sample News Title";
        String content = "This is a sample news content.";

        try {
            News.create(title, null, content);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("내용이 null 혹은 공백인 경우 예외를 반환한다.")
    void newsCreationTestWithInvalidContent() {
        String title = "Sample News Title";
        Video video = VideoFixture.createDefault();

        try {
            News.create(title, video, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }

        try {
            News.create(title, video, "");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

}