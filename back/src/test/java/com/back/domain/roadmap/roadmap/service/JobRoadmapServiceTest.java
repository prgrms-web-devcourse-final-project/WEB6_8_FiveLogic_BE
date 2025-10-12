package com.back.domain.roadmap.roadmap.service;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.service.JobService;
import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapListResponse;
import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapResponse;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.service.TaskService;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class JobRoadmapServiceTest {

    @Autowired
    private JobRoadmapService jobRoadmapService;

    @Autowired
    private JobRoadmapRepository jobRoadmapRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskService taskService;

    private Job testJob;
    private Task testTask1;
    private Task testTask2;
    private JobRoadmap testJobRoadmap;

    @BeforeEach
    void setUp() {
        // 기존 Job 조회 또는 새로 생성 (UNIQUE 제약 조건 회피)
        try {
            testJob = jobService.create("테스트용 백엔드 개발자", "테스트용 서버 사이드 개발을 담당하는 개발자");
        } catch (Exception e) {
            // 이미 존재할 경우 조회로 대체하거나 다른 이름 사용
            testJob = jobService.create("테스트 백엔드 " + System.currentTimeMillis(), "테스트용 서버 사이드 개발자");
        }

        // 테스트용 Task 생성 (UNIQUE 제약 조건 회피)
        try {
            testTask1 = taskService.create("Test Java");
            testTask2 = taskService.create("Test Spring Boot");
        } catch (Exception e) {
            // 이미 존재할 경우 타임스탬프 추가
            long timestamp = System.currentTimeMillis();
            testTask1 = taskService.create("Java_" + timestamp);
            testTask2 = taskService.create("SpringBoot_" + timestamp);
        }

        // 테스트용 JobRoadmap 생성
        testJobRoadmap = JobRoadmap.builder()
                .job(testJob)
                .build();
        testJobRoadmap = jobRoadmapRepository.save(testJobRoadmap);

        // 테스트용 RoadmapNode 생성 (트리 구조)
        RoadmapNode rootNode = RoadmapNode.builder()
                .roadmapId(testJobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(testTask1)
                .taskName(testTask1.getName())
                .learningAdvice("Java 프로그래밍 언어")
                .stepOrder(1)
                .level(0)
                .build();

        RoadmapNode childNode = RoadmapNode.builder()
                .roadmapId(testJobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(testTask2)
                .taskName(testTask2.getName())
                .learningAdvice("Spring Boot 프레임워크")
                .stepOrder(1)
                .level(1)
                .build();
        rootNode.addChild(childNode);

        testJobRoadmap.getNodes().add(rootNode);
        testJobRoadmap.getNodes().add(childNode);
        jobRoadmapRepository.save(testJobRoadmap);
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 전체 조회")
    void getAllJobRoadmaps() {
        // when
        List<JobRoadmapListResponse> result = jobRoadmapService.getAllJobRoadmaps();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(6);

        JobRoadmapListResponse response = result.get(5); // 마지막에 추가된 테스트 데이터
        assertThat(response.id()).isEqualTo(testJobRoadmap.getId());
        assertThat(response.jobName()).isEqualTo(testJob.getName());
        assertThat(response.jobDescription()).isEqualTo(testJob.getDescription());
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 페이징 및 키워드 검색")
    void getJobRoadmaps_WithPagingAndKeyword() {
        // given
        String keyword = "테스트";
        int page = 0;
        int size = 10;

        // when
        Page<JobRoadmapListResponse> result = jobRoadmapService.getJobRoadmaps(keyword, page, size);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        JobRoadmapListResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(testJobRoadmap.getId());
        assertThat(response.jobName()).isEqualTo(testJob.getName());
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - 키워드 검색 결과 없음")
    void getJobRoadmaps_NoResults() {
        // given
        String keyword = "존재하지않는키워드";
        int page = 0;
        int size = 10;

        // when
        Page<JobRoadmapListResponse> result = jobRoadmapService.getJobRoadmaps(keyword, page, size);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("직업 로드맵 다건 조회 - null 키워드")
    void getJobRoadmaps_NullKeyword() {
        // given
        String keyword = null;
        int page = 0;
        int size = 10;

        // when
        Page<JobRoadmapListResponse> result = jobRoadmapService.getJobRoadmaps(keyword, page, size);

        // then
        assertThat(result.getContent()).hasSize(6);
        assertThat(result.getTotalElements()).isEqualTo(6);
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 성공")
    void getJobRoadmapById_Success() {
        // when
        JobRoadmapResponse result = jobRoadmapService.getJobRoadmapById(testJobRoadmap.getId());

        // then
        assertThat(result.id()).isEqualTo(testJobRoadmap.getId());
        assertThat(result.jobId()).isEqualTo(testJob.getId());
        assertThat(result.jobName()).isEqualTo(testJob.getName());
        assertThat(result.totalNodeCount()).isEqualTo(2);

        // 트리 구조 검증
        assertThat(result.nodes()).hasSize(1); // 루트 노드 1개

        // 루트 노드 검증
        var rootNode = result.nodes().get(0);
        assertThat(rootNode.taskName()).isEqualTo(testTask1.getName());
        assertThat(rootNode.level()).isEqualTo(0);
        assertThat(rootNode.stepOrder()).isEqualTo(1);
        assertThat(rootNode.parentId()).isNull();

        // 자식 노드 검증
        assertThat(rootNode.children()).hasSize(1);
        var childNode = rootNode.children().get(0);
        assertThat(childNode.taskName()).isEqualTo(testTask2.getName());
        assertThat(childNode.level()).isEqualTo(1);
        assertThat(childNode.stepOrder()).isEqualTo(1);
        assertThat(childNode.parentId()).isEqualTo(rootNode.id());
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 존재하지 않는 ID")
    void getJobRoadmapById_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> jobRoadmapService.getJobRoadmapById(nonExistentId))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("직업 로드맵을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("직업 로드맵 단건 조회 - 트리 구조 정렬 검증")
    void getJobRoadmapById_TreeStructureSorting() {
        // given - 추가 노드들로 복잡한 트리 구조 생성
        long timestamp = System.currentTimeMillis();
        Task testTask3 = taskService.create("MySQL_" + timestamp);
        Task testTask4 = taskService.create("Git_" + timestamp);

        // 같은 레벨에 여러 노드 추가
        RoadmapNode rootNode2 = RoadmapNode.builder()
                .roadmapId(testJobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(testTask4)
                .taskName(testTask4.getName())
                .learningAdvice("버전 관리 시스템")
                .stepOrder(2)
                .level(0)
                .build();

        RoadmapNode childNode2 = RoadmapNode.builder()
                .roadmapId(testJobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(testTask3)
                .taskName(testTask3.getName())
                .learningAdvice("관계형 데이터베이스")
                .stepOrder(2)
                .level(1)
                .build();
        testJobRoadmap.getNodes().get(0).addChild(childNode2); // Java 노드의 자식

        testJobRoadmap.getNodes().add(rootNode2);
        testJobRoadmap.getNodes().add(childNode2);
        jobRoadmapRepository.save(testJobRoadmap);

        // when
        JobRoadmapResponse result = jobRoadmapService.getJobRoadmapById(testJobRoadmap.getId());

        // then
        assertThat(result.totalNodeCount()).isEqualTo(4);
        assertThat(result.nodes()).hasSize(2); // 루트 노드 2개

        // 루트 노드 정렬 검증 (level -> stepOrder 순)
        assertThat(result.nodes().get(0).taskName()).isEqualTo(testTask1.getName());  // level=0, stepOrder=1
        assertThat(result.nodes().get(1).taskName()).isEqualTo(testTask4.getName());   // level=0, stepOrder=2

        // Java 노드의 자식 노드 정렬 검증
        var javaNode = result.nodes().get(0);
        assertThat(javaNode.children()).hasSize(2);
        assertThat(javaNode.children().get(0).taskName()).isEqualTo(testTask2.getName()); // stepOrder=1
        assertThat(javaNode.children().get(1).taskName()).isEqualTo(testTask3.getName());       // stepOrder=2
    }
}