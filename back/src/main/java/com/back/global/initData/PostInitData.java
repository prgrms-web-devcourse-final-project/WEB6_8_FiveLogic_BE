package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.repository.PostCommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
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
    private final MemberService memberService;
    private final PostCommentRepository postCommentRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("postinit데이터 생성");
        initDateForPostDataAndPostCommentData();


        log.info("postRepo개수는 " + postRepository.count());

        log.info("postinit데이터 생성 완료");
    }


    @Transactional
    protected void initDateForPostDataAndPostCommentData() {
        if (postRepository.count() > 0) return;

        Member member2 = memberService.joinMentee("user2", "사용자1", "nickname1","password123","");
        Member member3 = memberService.joinMentee("user3", "사용자1", "nickname2","password123", "");
        Member member4 = memberService.joinMentee("user4", "사용자1", "nickname3","password123", "");

        // 여러 종류의 게시글 생성
        createPost("정보글 제목", "정보글 내용", member2, Post.PostType.INFORMATIONPOST);
        createPost("연습글 제목", "연습글 내용", member3, Post.PostType.PRACTICEPOST);
        createPost("질문글 제목", "질문글 내용", member4, Post.PostType.QUESTIONPOST);

        createComment(member2, 1L, "1번댓글");
        createComment(member2, 1L, "2댓글");
        createComment(member2, 1L, "3번댓글");
        createComment(member2, 1L, "4번댓글");
        createComment(member2, 1L, "5번댓글");
        createComment(member2, 1L, "6번댓글");
        createComment(member2, 1L, "7번댓글");


    }

    private void createPost(String title, String content, Member member, Post.PostType type) {
        validPostType(String.valueOf(type));

        Post post = Post.builder()
                        .title(title)
                        .content(content)
                        .member(member)
                        .postType(type)
                        .build();

        postRepository.save(post);
    }

    public void createComment(Member member, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ServiceException("400", "해당 Id의 게시글이 없습니다."));

        PostComment postComment = PostComment.builder()
                .post(post)
                .content(content)
                .member(member)
                .role(member.getRole().name())
                .build();

        postCommentRepository.save(postComment);

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
