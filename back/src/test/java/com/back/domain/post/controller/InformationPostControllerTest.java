package com.back.domain.post.controller;

import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
    private MockMvc mvc;

    @Test
    @DisplayName("게시글 생성")
    void createPost() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/post/infor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "memberId": 123,
                                            "postType": "informationPost",
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
}
