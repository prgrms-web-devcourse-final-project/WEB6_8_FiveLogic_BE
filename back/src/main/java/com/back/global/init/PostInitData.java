package com.back.global.init;

import com.back.domain.post.entity.Post;
import com.back.domain.post.entity.PracticePost;
import com.back.domain.post.entity.QuestionPost;
import com.back.domain.post.repository.PostRepository;
import com.back.domain.post.repository.PracticePostRepository;
import com.back.domain.post.repository.QuestionPostRepository;
import com.back.global.exception.ServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
@RequiredArgsConstructor
@Slf4j
public class PostInitData implements ApplicationRunner {
    private final PostRepository postRepository;

    private final PracticePostRepository practicePostRepository;
    private final QuestionPostRepository questionPostRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("postinit데이터 생성");
        initPostData();
        long count = practicePostRepository.count();
        log.info("PracticePost개수는 " + count);
        log.info("postRepo개수는 " + postRepository.count());
        log.info("questionPostRepo 개수는 " + questionPostRepository.count());
        log.info("postinit데이터 생성 완료");
    }

    @Transactional
    protected void initPostData() {
        if (postRepository.count() > 0) return;

        // 여러 종류의 게시글 생성
        createPost("정보글 제목", "정보글 내용", Post.PostType.INFORMATIONPOST);
        createPost("연습글 제목", "연습글 내용", Post.PostType.PRACTICEPOST);
        createPost("질문글 제목", "질문글 내용", Post.PostType.QUESTIONPOST);
    }

    private void createPost(String title, String content, Post.PostType type) {
        validPostType(String.valueOf(type));

        Post post = createPostByType(type);
        post.setTitle(title);
        post.setContent(content);
        post.setAuthorName("테스트유저");
        post.setPostType(type);

        saveByType(post);
    }

    /// ///////

    private Post createPostByType(Post.PostType postType) {
        return switch (postType) {
            case INFORMATIONPOST -> new Post();
            case PRACTICEPOST -> new PracticePost();
            case QUESTIONPOST -> new QuestionPost();
        };
    }

    private Post saveByType(Post post) {
        return switch (post.getPostType()) {
            case INFORMATIONPOST -> postRepository.save(post);
            case PRACTICEPOST -> practicePostRepository.save((PracticePost) post);
            case QUESTIONPOST -> questionPostRepository.save((QuestionPost) post);
        };
    }

    private void validPostType(String postTypeStr) {
        boolean eq = false;

        String[] validType = new String[3];
        validType[0] = "INFORMATIONPOST";
        validType[1] = "PRACTICEPOST";
        validType[2] = "QUESTIONPOST";

        for(String x : validType) if(x.equals(postTypeStr)) eq = true;

        if(!eq) throw new ServiceException("400-2", "유효하지 않은 PostType입니다.");
    }
}
