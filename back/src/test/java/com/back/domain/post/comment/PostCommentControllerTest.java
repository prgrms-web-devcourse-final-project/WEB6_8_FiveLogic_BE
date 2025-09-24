package com.back.domain.post.comment;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.comment.controller.PostCommentController;
import com.back.domain.post.comment.entity.PostComment;
import com.back.domain.post.comment.repository.PostCommentRepository;
import com.back.domain.post.comment.service.PostCommentService;
import com.back.global.security.SecurityUser;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostCommentControllerTest {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        Member member = memberService.joinMentee("user1", "사용자1", "nickname4","password123", "" );

        // SecurityContext에 인증 정보 설정
        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getName(),
                member.getNickname(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                securityUser.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("댓글 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/comment/{post_id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                           "memberId": 123,
                                           "postId": 1,
                                           "role": "mentor", 
                                           "comment": "댓글 내용"
                                         }
                                        """.stripIndent())
                )
                .andDo(print());

        // 실제 생성된 게시글 조회 (실제 DB에서)
        PostComment createdPost = postCommentRepository.findById(1L).get();

        resultActions
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("createComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("댓글 작성 완료"));
    }

}
