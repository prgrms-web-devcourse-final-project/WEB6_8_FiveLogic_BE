package com.back.domain.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
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
public class InformationPostControllerTest {

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        Member member = memberService.join("user1", "사용자1", "password123", Member.Role.MENTEE);

        // SecurityContext에 인증 정보 설정
        SecurityUser securityUser = new SecurityUser(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getName(),
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
    @DisplayName("게시글 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/infor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "memberId": 1,
                                            "postType": "INFORMATIONPOST",
                                            "title": "테스트 제목",
                                            "content": "테스트 내용"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // 실제 생성된 게시글 조회 (실제 DB에서)
        Post createdPost = postService.findByid(1L);

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("createPost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다. "))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.postId").value(createdPost.getId()))
                .andExpect(jsonPath("$.data.title").value(createdPost.getTitle()));
    }

    @Test
    @DisplayName("일치하지 않는 postType")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/infor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "memberId": 3,
                                            "postType": "INFORMATIONPOST12",
                                            "title": "테스트 제목",
                                            "content": "테스트 내용"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("createPost"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("유효하지 않은 PostType입니다."));
    }
}
