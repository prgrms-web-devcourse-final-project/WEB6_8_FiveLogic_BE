package com.back.fixture.Post;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import org.springframework.test.util.ReflectionTestUtils;

public class PostFixture {
    private static Post.PostType questionPost = Post.PostType.QUESTIONPOST;
    private static final Post.PostType practicePost = Post.PostType.PRACTICEPOST;
    private static final Post.PostType InformationPost = Post.PostType.INFORMATIONPOST;
    private static final String title = "테스트 제목";
    private static final String content = "테스트 내용";
    private static final String job = "백엔드";



    public static Post createQuestionPost(Member member) {
        Post post = Post.builder()
                .postType(questionPost)
                .title(title)
                .content(content)
                .member(member)
                .build();

        post.updateResolveStatus(false);

        return post;
    }

    public static Post createPracticePost(Member member) {
        Post post = Post.builder()
                .postType(practicePost)
                .title(title)
                .content(content)
                .member(member)
                .build();

        post.updateJob(job);

        return post;
    }

    public static Post createInformationPost(Member member, Long id) {
        Post post = Post.builder()
                .postType(InformationPost)
                .title(title)
                .content(content)
                .member(member)
                .build();

        ReflectionTestUtils.setField(post, "id", id);

        return post;
    }
}
