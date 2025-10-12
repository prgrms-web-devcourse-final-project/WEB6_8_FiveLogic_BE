package com.back.domain.roadmap.roadmap.controller;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.service.JobService;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class JobRoadmapControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JobRoadmapRepository jobRoadmapRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskService taskService;

    private Job testJob1;
    private Job testJob2;
    private JobRoadmap testJobRoadmap1;
    private JobRoadmap testJobRoadmap2;
    private Task javaTask;
    private Task springTask;
    private Task reactTask;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 테스트용 Job 생성
        long timestamp = System.currentTimeMillis();
        testJob1 = jobService.create("테스트 백엔드_" + timestamp, "테스트용 서버 사이드 개발자");
        testJob2 = jobService.create("테스트 프론트엔드_" + timestamp, "테스트용 클라이언트 사이드 개발자");

        // 테스트용 Task 생성
        javaTask = taskService.create("TestJava_" + timestamp);
        springTask = taskService.create("TestSpring_" + timestamp);
        reactTask = taskService.create("TestReact_" + timestamp);

        // 테스트용 JobRoadmap 1 생성 (백엔드)
        testJobRoadmap1 = JobRoadmap.builder()
                .job(testJob1)
                .build();
        testJobRoadmap1 = jobRoadmapRepository.save(testJobRoadmap1);

        // 백엔드 로드맵 노드 생성
        RoadmapNode javaNode = RoadmapNode.builder()
                .roadmapId(testJobRoadmap1.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(javaTask)
                .taskName(javaTask.getName())
                .learningAdvice("Java 프로그래밍 언어")
                .stepOrder(1)
                .level(0)
                .build();

        RoadmapNode springNode = RoadmapNode.builder()
                .roadmapId(testJobRoadmap1.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(springTask)
                .taskName(springTask.getName())
                .learningAdvice("Spring Boot 프레임워크")
                .stepOrder(1)
                .level(1)
                .build();
        javaNode.addChild(springNode);

        testJobRoadmap1.getNodes().add(javaNode);
        testJobRoadmap1.getNodes().add(springNode);
        jobRoadmapRepository.save(testJobRoadmap1);

        // 테스트용 JobRoadmap 2 생성 (프론트엔드)
        testJobRoadmap2 = JobRoadmap.builder()
                .job(testJob2)
                .build();
        testJobRoadmap2 = jobRoadmapRepository.save(testJobRoadmap2);

        RoadmapNode reactNode = RoadmapNode.builder()
                .roadmapId(testJobRoadmap2.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(reactTask)
                .taskName(reactTask.getName())
                .learningAdvice("React 라이브러리")
                .stepOrder(1)
                .level(0)
                .build();

        testJobRoadmap2.getNodes().add(reactNode);
        jobRoadmapRepository.save(testJobRoadmap2);
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 기본 페이징")
    void getJobRoadmaps_DefaultPaging() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("직업 로드맵 목록 조회 성공"))
                .andExpect(jsonPath("$.data.jobRoadmaps").isArray())
                .andExpect(jsonPath("$.data.jobRoadmaps.length()").value(7))
                .andExpect(jsonPath("$.data.totalElements").value(7))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 페이지 크기 지정")
    void getJobRoadmaps_CustomPageSize() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps")
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobRoadmaps.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(7))
                .andExpect(jsonPath("$.data.totalPage").value(7))
                .andExpect(jsonPath("$.data.currentPage").value(0));
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 키워드 검색")
    void getJobRoadmaps_WithKeyword() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps")
                        .param("keyword", "테스트")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobRoadmaps.length()").value(2))
                .andExpect(jsonPath("$.data.jobRoadmaps[0].jobName").value(testJob1.getName()));
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 존재하지 않는 키워드")
    void getJobRoadmaps_NoResultsKeyword() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps")
                        .param("keyword", "존재하지않는키워드")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobRoadmaps.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 응답 필드 검증")
    void getJobRoadmaps_ResponseFields() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobRoadmaps[0].id").exists())
                .andExpect(jsonPath("$.data.jobRoadmaps[0].jobName").exists())
                .andExpect(jsonPath("$.data.jobRoadmaps[0].jobDescription").exists())
                .andExpect(jsonPath("$.data.jobRoadmaps[0].id").isNumber())
                .andExpect(jsonPath("$.data.jobRoadmaps[0].jobName").isString())
                .andExpect(jsonPath("$.data.jobRoadmaps[0].jobDescription").isString());
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 성공")
    void getJobRoadmapById_Success() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps/{id}", testJobRoadmap1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("직업 로드맵 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(testJobRoadmap1.getId()))
                .andExpect(jsonPath("$.data.jobId").value(testJob1.getId()))
                .andExpect(jsonPath("$.data.jobName").value(testJob1.getName()))
                .andExpect(jsonPath("$.data.totalNodeCount").value(2));
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 트리 구조 검증")
    void getJobRoadmapById_TreeStructure() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps/{id}", testJobRoadmap1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // 루트 노드 검증
                .andExpect(jsonPath("$.data.nodes.length()").value(1))
                .andExpect(jsonPath("$.data.nodes[0].taskName").value(javaTask.getName()))
                .andExpect(jsonPath("$.data.nodes[0].level").value(0))
                .andExpect(jsonPath("$.data.nodes[0].stepOrder").value(1))
                .andExpect(jsonPath("$.data.nodes[0].parentId").isEmpty())
                // 자식 노드 검증
                .andExpect(jsonPath("$.data.nodes[0].children.length()").value(1))
                .andExpect(jsonPath("$.data.nodes[0].children[0].taskName").value(springTask.getName()))
                .andExpect(jsonPath("$.data.nodes[0].children[0].level").value(1))
                .andExpect(jsonPath("$.data.nodes[0].children[0].stepOrder").value(1))
                .andExpect(jsonPath("$.data.nodes[0].children[0].parentId").exists());
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 존재하지 않는 ID")
    void getJobRoadmapById_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when & then
        mvc.perform(get("/job-roadmaps/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 모든 응답 필드 검증")
    void getJobRoadmapById_AllResponseFields() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps/{id}", testJobRoadmap1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.jobId").isNumber())
                .andExpect(jsonPath("$.data.jobName").isString())
                .andExpect(jsonPath("$.data.nodes").isArray())
                .andExpect(jsonPath("$.data.totalNodeCount").isNumber())
                .andExpect(jsonPath("$.data.createdDate").exists())
                .andExpect(jsonPath("$.data.modifiedDate").exists());
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 노드 필드 검증")
    void getJobRoadmapById_NodeFields() throws Exception {
        // when & then
        mvc.perform(get("/job-roadmaps/{id}", testJobRoadmap1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes[0].id").isNumber())
                .andExpect(jsonPath("$.data.nodes[0].taskId").isNumber())
                .andExpect(jsonPath("$.data.nodes[0].taskName").isString())
                .andExpect(jsonPath("$.data.nodes[0].learningAdvice").isString())
                .andExpect(jsonPath("$.data.nodes[0].stepOrder").isNumber())
                .andExpect(jsonPath("$.data.nodes[0].level").isNumber())
                .andExpect(jsonPath("$.data.nodes[0].isLinkedToTask").isBoolean())
                .andExpect(jsonPath("$.data.nodes[0].children").isArray());
    }
}