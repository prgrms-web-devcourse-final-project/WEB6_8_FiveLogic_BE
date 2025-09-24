package com.back.domain.roadmap.task.controller;

import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class TaskControllerTest {
    @Autowired
    private TaskService taskService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // BaseInitData에 추가로 테스트용 데이터 생성
        setupAdditionalTestData();
    }

    @Test
    @DisplayName("키워드로 Task 검색 - Java 직접 매치")
    void t1() throws Exception {
        String keyword = "Java";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("searchTasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].name").isString())
                //.andExpect(jsonPath("$.data.length()").value(2))
                //.andExpect(jsonPath("$.data[*].name", hasItems("Java", "JavaScript")))
        ;
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 한글 별칭으로 검색")
    void t2() throws Exception {
        String keyword = "자바";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("searchTasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                //.andExpect(jsonPath("$.data.length()").value(2))
                //.andExpect(jsonPath("$.data[*].name", hasItems("Java", "JavaScript")))
        ;
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 대소문자 무시")
    void t3() throws Exception {
        String keyword = "REACT";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                //.andExpect(jsonPath("$.data.length()").value(1))
                //.andExpect(jsonPath("$.data[0].name").value("React"))
        ;
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 검색 결과 없음")
    void t4() throws Exception {
        String keyword = "nonexistent";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 빈 키워드")
    void t5() throws Exception {
        String keyword = "";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("검색 결과가 없습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 공백만 있는 키워드")
    void t6() throws Exception {
        String keyword = "   ";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("검색 결과가 없습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("키워드로 Task 검색 - null 키워드")
    void t7() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                        // keyword 파라미터 없음
                )
                .andDo(print());

        // @RequestParam으로 필수 파라미터가 없으면 400 에러
        resultActions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 앞뒤 공백 제거")
    void t8() throws Exception {
        String keywordWithSpaces = "  java  ";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keywordWithSpaces)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                //.andExpect(jsonPath("$.data.length()").value(2))
                //.andExpect(jsonPath("$.data[*].name", hasItems("Java", "JavaScript")))
        ;
    }

    @Test
    @DisplayName("키워드로 Task 검색 - 특수문자 포함")
    void t9() throws Exception {
        String keyword = "C++";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("C++"));
    }

    @Test
    @DisplayName("키워드로 Task 검색 - pending alias는 결과에서 제외")
    void t10() throws Exception {
        String keyword = "미지";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/tasks/search")
                                .param("keyword", keyword)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Task 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Pending Alias 생성 - 성공")
    void t11() throws Exception {
        String requestBody = """
            {
                "taskName": "새로운기술"
            }
            """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/tasks/aliases/pending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("createPendingAlias"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.msg").value("새로운 Pending Alias 등록 성공. 관리자 검토 후 매칭 또는 새로운 Task로 등록됩니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.aliasId").exists())
                .andExpect(jsonPath("$.data.aliasId").isNumber())
                .andExpect(jsonPath("$.data.aliasName").value("새로운기술"))
                .andExpect(jsonPath("$.data.createTime").exists());
    }

    @Test
    @DisplayName("Pending Alias 생성 - 이미 등록된 Task와 중복")
    void t12() throws Exception {
        String requestBody = """
            {
                "taskName": "Java"
            }
            """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/tasks/aliases/pending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("createPendingAlias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("이미 등록된 task입니다."));
    }

    @Test
    @DisplayName("Pending Alias 생성 - 이미 등록된 Task의 별칭과 중복")
    void t13() throws Exception {
        String requestBody = """
            {
                "taskName": "자바"
            }
            """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/tasks/aliases/pending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("createPendingAlias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("이미 등록된 task의 별칭입니다."));
    }

    @Test
    @DisplayName("Pending Alias 생성 - 이미 제안된 Pending Alias와 중복")
    void t14() throws Exception {
        // Given: 먼저 pending alias를 하나 생성
        taskService.createPendingAlias("중복테스트");

        String requestBody = """
            {
                "taskName": "중복테스트"
            }
            """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/tasks/aliases/pending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("createPendingAlias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("이미 제안된 task입니다."));
    }

    @Test
    @DisplayName("Pending Alias 생성 - 유효성 검사 실패 (빈 문자열)")
    void t15() throws Exception {
        String requestBody = """
            {
                "taskName": ""
            }
            """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/tasks/aliases/pending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(TaskController.class))
                .andExpect(handler().methodName("createPendingAlias"))
                .andExpect(status().isBadRequest());
        // @Valid 검증 실패로 400 에러 발생 예상
    }

    @Test
    @DisplayName("Pending Alias 생성 - 유효성 검사 실패 (null)")
    void t16() throws Exception {
        String requestBody = """
            {
                "taskName": null
            }
            """;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/tasks/aliases/pending")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest());
        // @NotBlank 검증 실패
    }

    private void setupAdditionalTestData() {
        // 추가 테스트용 Task 생성
        Task cppTask = taskService.create("C++");
        taskService.createAlias(cppTask, "씨플플");

        // Pending alias 생성
        taskService.createPendingAlias("미지의 task");
    }

}