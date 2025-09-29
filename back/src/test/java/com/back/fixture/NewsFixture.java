package com.back.fixture;

import com.back.domain.file.video.entity.Video;
import com.back.domain.member.member.entity.Member;
import com.back.domain.news.comment.entity.NewsComment;
import com.back.domain.news.news.entity.News;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

public class NewsFixture {
    private Long id;
    private Member member = MemberFixture.createDefault();
    private String title = "Sample News Title";
    private Video video = VideoFixture.createDefault();
    private String content = "This is a sample news content.";
    private List<NewsComment> newsComments = new ArrayList<>();

    private static NewsFixture builder() {
        return new NewsFixture();
    }

    public static News createDefault() {
        return builder().build();
    }

    public static News create(Long id, Member member, String title) {
        News news = News.create(member, title, VideoFixture.createDefault(), "content");
        ReflectionTestUtils.setField(news, "id", id);
        return news;
    }

    public NewsFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public NewsFixture withMember(Member member) {
        this.member = member;
        return this;
    }

    public NewsFixture withTitle(String title) {
        this.title = title;
        return this;
    }

    public NewsFixture withVideo(Video video) {
        this.video = video;
        return this;
    }

    public NewsFixture withContent(String content) {
        this.content = content;
        return this;
    }

    public NewsFixture withComments(List<NewsComment> newsComments) {
        this.newsComments = newsComments;
        return this;
    }

    public News build() {
        News news = News.create(member, title, video, content);
        news.getNewsComment().addAll(newsComments);
        return news;
    }
}
