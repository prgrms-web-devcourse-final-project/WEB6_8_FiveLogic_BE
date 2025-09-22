package com.back.domain.news.like.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.news.news.entity.News;
import com.back.fixture.MemberFixture;
import com.back.fixture.NewsFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    @Test
    @DisplayName("멤버와 뉴스로 좋아요 객체 생성")
    void createLikeTest() {
        // given
        Member member = MemberFixture.createDefault();
        News news = NewsFixture.createDefault();

        // when
        Like like = Like.create(member, news);

        // then
        assertThat(like).isNotNull();
        assertThat(like.getMember()).isEqualTo(member);
        assertThat(like.getNews()).isEqualTo(news);
    }

    @Test
    @DisplayName("멤버가 null일 경우 예외가 발생한다.")
    void createLikeWithNullMemberTest() {
        // given
        News news = NewsFixture.createDefault();

        // when & then
        try {
            Like.create(null, news);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("Member cannot be null");
        }
    }

    @Test
    @DisplayName("뉴스가 null일 경우 예외가 발생한다.")
    void createLikeWithNullNewsTest() {
        // given
        Member member = MemberFixture.createDefault();

        // when & then
        try {
            Like.create(member, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("News cannot be null");
        }
    }
}
