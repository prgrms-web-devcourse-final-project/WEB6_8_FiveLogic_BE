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
    }

}