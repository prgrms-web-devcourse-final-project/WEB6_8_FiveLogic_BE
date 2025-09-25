package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        Member member = memberService.joinMentee("user1", "사용자1", "nickname19","password123", "");

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
    @DisplayName("게시글 생성")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/infor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "memberId": 1,
                                            "postType": "QUESTIONPOST",
                                            "title": "테스트 제목",
                                            "content": "테스트 내용"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // 실제 생성된 게시글 조회 (실제 DB에서)
        Post createdPost = postService.findById(4L);

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("createPost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.postId").value(createdPost.getId()))
                .andExpect(jsonPath("$.data.title").value(createdPost.getTitle()));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 제목 null")
    void t6() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/infor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "memberId": 1,
                                            "postType": "INFORMATIONPOST",
                                            "title": "",
                                            "content": "테스트 내용"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("createPost"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("title-NotBlank-제목은 null 혹은 공백일 수 없습니다."));
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

    @Test
    @DisplayName("게시글 조회 - 페이징 처리")
    void t3() throws Exception {
        // 테스트용 게시글 먼저 생성
        mvc.perform(
                post("/post/infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "memberId": 1,
                                "postType": "INFORMATIONPOST",
                                "title": "페이징 테스트 제목 1",
                                "content": "페이징 테스트 내용 1"
                            }
                            """)
        );

        mvc.perform(
                post("/post/infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "memberId": 1,
                                "postType": "INFORMATIONPOST",
                                "title": "페이징 테스트 제목 2",
                                "content": "페이징 테스트 내용 2"
                            }
                            """)
        );

        // 페이징 조회 테스트 - 기본값
        ResultActions resultActions = mvc
                .perform(
                        get("/post/infor")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("getPostWithPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.posts").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.msg").value("게시글이 조회 되었습니다."));
    }

    @Test
    @DisplayName("게시글 조회 - 페이징 처리 (파라미터 지정)")
    void t3_1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/post/infor")
                                .param("page", "0")
                                .param("size", "5")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("getPostWithPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.totalPage").exists())
                .andExpect(jsonPath("$.data.hasNext").exists());
    }

    @Test
    @DisplayName("게시글 조회 - 키워드 검색")
    void t3_2() throws Exception {
        // 검색 대상 게시글 생성
        mvc.perform(
                post("/post/infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "memberId": 1,
                                "postType": "INFORMATIONPOST",
                                "title": "Spring Boot 검색용 제목",
                                "content": "Spring Boot 검색용 내용"
                            }
                            """)
        );

        ResultActions resultActions = mvc
                .perform(
                        get("/post/infor")
                                .param("keyword", "Spring")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("getPostWithPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posts").isArray())
                .andExpect(jsonPath("$.msg").value("게시글이 조회 되었습니다."));
    }

    @Test
    @DisplayName("게시글 단건조회")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/post/infor/{post_id}", 1L)
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("getSinglePost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("게시글 단건 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("정보글 제목"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("게시글 단건조회 실패 - 유효하지 않은 Id")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/post/infor/{post_id}", 999L)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("getSinglePost"))
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("해당 Id의 게시글이 없습니다."));
    }

    @Test
    @DisplayName("게시글 삭제")
    void t7() throws Exception {
        mvc.perform(
                post("/post/infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "memberId": 1,
                                "postType": "INFORMATIONPOST",
                                "title": "삭제용 제목",
                                "content": "삭제용 내용"
                            }
                            """)
        );


        ResultActions resultActions = mvc
                .perform(
                        delete("/post/infor/{post_id}", 7L)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("removePost"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("게시글 삭제 성공"));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없는 사용자")
    void t8() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/post/infor/{post_id}", 3L)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("removePost"))
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("삭제 권한이 없습니다."));
    }

    @Test
    @DisplayName("게시글 수정")
    void t9() throws Exception {
        mvc.perform(
                post("/post/infor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "memberId": 1,
                                "postType": "INFORMATIONPOST",
                                "title": "삭제용 제목",
                                "content": "삭제용 내용"
                            }
                            """)
        );


        ResultActions resultActions = mvc
                .perform(
                        put("/post/infor/{post_id}", 8L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {                        
                                "title": "수정용 제목",
                                "content": "수정용 내용"
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("updatePost"))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("게시글 수정 성공"));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없는 사용자")
    void t10() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/post/infor/{post_id}", 2L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {                        
                                "title": "수정용 제목",
                                "content": "수정용 내용"
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("updatePost"))
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("수정 권한이 없습니다."));
    }

    @Test
    @DisplayName("게시글 수정 실패 - title blank")
    void t11() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/post/infor/{post_id}", 6L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {                        
                                "title": "",
                                "content": "수정용 내용"
                            }
                            """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(InformationPostController.class))
                .andExpect(handler().methodName("updatePost"))
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("title-NotBlank-제목은 null 혹은 공백일 수 없습니다."));
    }

}
