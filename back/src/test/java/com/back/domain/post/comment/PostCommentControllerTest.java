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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                        post("/post/{post_id}/comment", 1L)
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

    @Test
    @DisplayName("댓글 생성 실패 - comment blank")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/{post_id}/comment", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                           "memberId": 123,
                                           "postId": 1,
                                           "role": "mentor",
                                           "comment": ""
                                         }
                                        """.stripIndent())
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("createComment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("comment-NotBlank-댓글을 입력해주세요"));
    }


    @Test
    @DisplayName("댓글 다건조회")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/post/post/{post_id}", 1L)
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("getAllPostComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.msg").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));

    }

    @Test
    @DisplayName("댓글 삭제")
    void t4() throws Exception {
        mvc
                .perform(
                        post("/post/{post_id}/comment", 2L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                           "memberId": 7,
                                           "postId": 2,
                                           "role": "mentor",
                                           "comment": "댓글 내용"
                                         }
                                        """)
                );




        // 댓글 삭제
        ResultActions resultActions = mvc
                .perform(
                        delete("/post/{post_id}/comment", 2L)  // URL 수정
                                .contentType(MediaType.APPLICATION_JSON)   // Content-Type 추가
                                .content("""
                                    {
                                       "commentId": 9
                                     }
                                    """)  // Request Body 추가
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())  // 먼저 상태 확인
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("removePostComment"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("댓글 삭제 성공"));
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없는 사용자")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/post/{post_id}/comment", 1L)  // URL 수정
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                       "commentId": 1
                                     }
                                    """)  // Request Body 추가
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())  // 먼저 상태 확인
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("removePostComment"))
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("삭제 권한이 없습니다."));
    }

    @Test
    @DisplayName("댓글 수정")
    void t6() throws Exception {
        mvc
                .perform(
                        post("/post/{post_id}/comment", 2L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                           "memberId": 7,
                                           "postId": 2,
                                           "role": "mentor",
                                           "comment": "댓글 내용"
                                         }
                                        """)
                );


        ResultActions resultActions = mvc
                .perform(
                        put("/post/{post_id}/comment", 2L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {                        
                                "commentId": 10,
                                "content": "수정용 내용"
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("updatePostComment"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("댓글 수정 성공"));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없는 사용자 ")
    void t7() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/post/{post_id}/comment", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {                        
                                "commentId": 1,
                                "content": "수정용 내용"
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("updatePostComment"))
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("수정 권한이 없습니다."));
    }

    @Test
    @DisplayName("댓글 수정 실패 - Blank Content")
    void t8() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/post/{post_id}/comment", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {                        
                                "commentId": 1,
                                "content": ""
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PostCommentController.class))
                .andExpect(handler().methodName("updatePostComment"))
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("content-NotBlank-공백일 수 없습니다."));
    }

}
