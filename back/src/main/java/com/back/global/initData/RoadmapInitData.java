package com.back.global.initData;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.repository.JobRepository;
import com.back.domain.job.job.service.JobService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.member.mentor.repository.MentorRepository;
import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapSaveRequest;
import com.back.domain.roadmap.roadmap.dto.request.RoadmapNodeRequest;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.roadmap.service.JobRoadmapIntegrationService;
import com.back.domain.roadmap.roadmap.service.MentorRoadmapService;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.domain.roadmap.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Transactional
public class RoadmapInitData {
    private final JobService jobService;
    private final JobRepository jobRepository;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final MemberService memberService;
    private final MentorRepository mentorRepository;
    private final MentorRoadmapService mentorRoadmapService;
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final JobRoadmapRepository jobRoadmapRepository;
    private final JobRoadmapIntegrationService jobRoadmapIntegrationService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner2() {
        return args -> runInitData();
    }

    @Transactional
    public void runInitData() {
        initJobData();
        initTaskData();           // 보강된 Task 목록
        //initSampleMentorRoadmaps(); // 활성화: 다양한 멘토 로드맵 생성
        //initSampleJobRoadmap();   // 직업 로드맵 조회 API 테스트용 샘플 데이터

        // 통합 로직 테스트
        //initTestMentorRoadmaps();    // 테스트용 멘토 로드맵 10개 생성
        //testJobRoadmapIntegration(); // 통합 로직 실행 및 트리 구조 출력
    }

    // --- Job 초기화 ---
    public void initJobData() {
        if (jobService.count() > 0) return;

        Job job1 = jobService.create("백엔드 개발자", "서버 사이드 로직 구현과 데이터베이스를 담당하는 개발자");
        jobService.createAlias(job1, "백엔드");
        jobService.createAlias(job1, "BE 개발자");
        jobService.createAlias(job1, "Backend 개발자");
        jobService.createAlias(job1, "서버 개발자");
        jobService.createAlias(job1, "API 개발자");

        Job job2 = jobService.create("프론트엔드 개발자", "사용자 인터페이스(UI)와 사용자 경험(UX)을 담당하는 개발자");
        jobService.createAlias(job2, "프론트엔드");
        jobService.createAlias(job2, "FE 개발자");
        jobService.createAlias(job2, "Frontend 개발자");
        jobService.createAlias(job2, "웹 퍼블리셔");
        jobService.createAlias(job2, "UI 개발자");
        jobService.createAlias(job2, "클라이언트 개발자");
    }

    // --- Task 초기화 (기존 + 기초 보강) ---
    public void initTaskData() {
        if (taskService.count() > 0) return;

        // 언어
        Task java = taskService.create("Java");
        Task python = taskService.create("Python");
        Task javascript = taskService.create("JavaScript");
        Task go = taskService.create("Go");
        Task kotlin = taskService.create("Kotlin");
        Task csharp = taskService.create("C#");

        // 프레임워크 / 백엔드 스택
        Task springBoot = taskService.create("Spring Boot");
        Task springMvc = taskService.create("Spring MVC");
        Task springSecurity = taskService.create("Spring Security");
        Task jpa = taskService.create("JPA");
        Task django = taskService.create("Django");
        Task fastapi = taskService.create("FastAPI");
        Task expressjs = taskService.create("Express.js");
        Task nodejs = taskService.create("Node.js");

        // DB
        Task mysql = taskService.create("MySQL");
        Task postgresql = taskService.create("PostgreSQL");
        Task mongodb = taskService.create("MongoDB");
        Task redis = taskService.create("Redis");
        Task oracle = taskService.create("Oracle");

        // 빌드 / 테스트
        Task gradle = taskService.create("Gradle");
        Task maven = taskService.create("Maven");
        Task junit = taskService.create("JUnit");
        Task mockito = taskService.create("Mockito");
        Task postman = taskService.create("Postman");

        // DevOps / Infra
        Task git = taskService.create("Git");
        Task docker = taskService.create("Docker");
        Task kubernetes = taskService.create("Kubernetes");
        Task aws = taskService.create("AWS");
        Task jenkins = taskService.create("Jenkins");
        Task nginx = taskService.create("Nginx");
        Task linux = taskService.create("Linux");

        // Frontend 관련 (풀스택 후보용)
        Task htmlCss = taskService.create("HTML/CSS");
        Task react = taskService.create("React");
        Task vue = taskService.create("Vue.js");
        Task typescript = taskService.create("TypeScript");
        Task nextjs = taskService.create("Next.js");

        // 기초 / 공통 주제 (표준화된 학습 목표로서 Task로 유지)
        Task programmingFundamentals = taskService.create("Programming Fundamentals"); // 변수/제어문/함수 등
        Task algorithmBasics = taskService.create("Algorithm Basics");
        Task terminal = taskService.create("Terminal / CLI");
        Task http = taskService.create("HTTP");
        Task restApi = taskService.create("REST API");
        Task dataModeling = taskService.create("Data Modeling");
        Task debugging = taskService.create("Debugging Basics");
        Task ciCd = taskService.create("CI/CD");
        Task monitoring = taskService.create("Monitoring");
        Task logging = taskService.create("Logging");
        Task kafka = taskService.create("Kafka");
        Task rabbitmq = taskService.create("RabbitMQ");
        Task graphql = taskService.create("GraphQL");
        Task caching = taskService.create("Caching");
        Task testing = taskService.create("Testing");
        Task securityBasics = taskService.create("Security Basics");
        Task performance = taskService.create("Performance Tuning");

        // Alias 예시 (자주 쓰이는 동의어 등록)
        taskService.createAlias(restApi, "API");
        taskService.createAlias(java, "자바");
        taskService.createAlias(javascript, "JS");
        taskService.createAlias(javascript, "자바스크립트");
        taskService.createAlias(python, "파이썬");
        taskService.createAlias(django, "장고");
        taskService.createAlias(mongodb, "몽고DB");
        taskService.createAlias(redis, "레디스");
        taskService.createAlias(kubernetes, "k8s");
        taskService.createAlias(kubernetes, "쿠버네티스");
        taskService.createAlias(git, "깃");
        taskService.createAlias(docker, "도커");
        taskService.createAlias(aws, "아마존웹서비스");
        taskService.createAlias(springBoot, "스프링부트");
        taskService.createAlias(springBoot, "스프링 부트");
        taskService.createAlias(springSecurity, "스프링 시큐리티");
        taskService.createAlias(programmingFundamentals, "프로그래밍 기초");
        taskService.createAlias(http, "HTTP 프로토콜");
    }

    // --- 전체 샘플 멘토 로드맵 생성 (기존 + 보강; 모든 로드맵 상단에 기초 노드 추가) ---
    public void initSampleMentorRoadmaps() {
        if (mentorRoadmapRepository.count() > 0) return;

        Job backendJob = jobRepository.findByName("백엔드 개발자")
                .orElseThrow(() -> new RuntimeException("백엔드 개발자 직업을 찾을 수 없습니다."));

        // 기존 샘플들 (원본 유지, 단 각 로드맵 앞에 기초 노드 추가)
        createJavaTraditionalRoadmap(backendJob);
        createJavaModernRoadmap(backendJob);
        createJavaEnterpriseRoadmap(backendJob);
        createPythonDjangoRoadmap(backendJob);
        createNodeJSRoadmap(backendJob);
        createFullStackRoadmap(backendJob);
        createDevOpsRoadmap(backendJob);

        // 추가 보강 샘플: 주니어/테스트중심/API-first/데이터중심/보안중심/프론트연계
        createJuniorBackendRoadmap(backendJob);
        createApiFirstRoadmap(backendJob);
        createDataFocusedRoadmap(backendJob);
        createSecurityFocusedRoadmap(backendJob);
        createTestingFocusedRoadmap(backendJob);
        createFrontendIntegrationRoadmap(backendJob);
    }

    // ---------------------- 기존 + 보강 샘플 로드맵들 ----------------------

    private void createJavaTraditionalRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor1@test.com", "멘토1", "test1", "1234", "백엔드 개발자", 3);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1,
                        "변수/제어문/함수/자료구조 기초. 권장 실습: 간단한 계산기 콘솔 앱 제작(30~60분)."),
                createNodeRequest("Git", 0, 2,
                        "버전 관리는 필수입니다. commit/branch/merge/Pull Request 실습. 권장 실습: GitHub에 리포지토리 올리기."),
                createNodeRequest("HTTP", 0, 3,
                        "요청/응답, 상태 코드 개념. 권장 실습: curl로 간단한 GET/POST 실습."),
                createNodeRequest("Java", 0, 4, "객체지향 프로그래밍 기초 및 실습 (OOP 핵심)"),
                createNodeRequest("Spring Boot", 0, 5, "자바 백엔드 표준 프레임워크 (간단한 REST API 만들기)"),
                createNodeRequest("MySQL", 0, 6, "기본 CRUD 및 스키마 설계 이해"),
                createNodeRequest("JPA", 0, 7, "ORM 기본: 엔티티/연관관계/지연로딩"),
                createNodeRequest("Spring Security", 0, 8, "인증/인가 기초 (JWT 패턴 등)"),
                createNodeRequest("JUnit", 0, 9, "단위 테스트 기반 실무 습관 (간단한 테스트 작성 권장)")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "전통적인 자바 백엔드 로드맵 (기초 포함)",
                "대기업/안정형 백엔드 스택을 목표로 한 로드맵. 각 노드에 작은 실습 권장 예시 포함.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createJavaModernRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor2@test.com", "멘토2", "test2", "1234", "백엔드 개발자", 5);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 문법/제어문/함수. 권장 실습: '간단한 계산기' 또는 '문자열 통계'"),
                createNodeRequest("Git", 0, 2, "Git Flow, PR 기반 협업 실습"),
                createNodeRequest("HTTP", 0, 3, "REST 원칙과 상태코드 실습"),
                createNodeRequest("Java", 0, 4, "Java 11+ 문법, Stream API"),
                createNodeRequest("Spring Boot", 0, 5, "Auto Configuration, Actuator 활용"),
                createNodeRequest("PostgreSQL", 0, 6, "JSONB 등 고급 기능"),
                createNodeRequest("Gradle", 0, 7, "빌드 스크립트 작성 실습"),
                createNodeRequest("Docker", 0, 8, "이미지 빌드 및 컨테이너 실행 실습"),
                createNodeRequest("Redis", 0, 9, "캐싱 패턴 실습"),
                createNodeRequest("AWS", 0, 10, "간단한 EC2 배포 실습 (권장)")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "모던 자바 백엔드 로드맵 (기초 포함)",
                "최신 기술 스택을 활용한 확장 가능한 자바 백엔드.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createJavaEnterpriseRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor3@test.com", "멘토3", "test3", "1234", "백엔드 개발자", 6);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검 및 알고리즘 기초"),
                createNodeRequest("Git", 0, 2, "엔터프라이즈 협업 워크플로 실습"),
                createNodeRequest("HTTP", 0, 3, "API 보안/성능 고려사항"),
                createNodeRequest("Java", 0, 4, "Java 17 LTS, 메모리/GC 기초"),
                createNodeRequest("Spring Boot", 0, 5, "프로파일/설정관리 심화"),
                createNodeRequest("Oracle", 0, 6, "PL/SQL과 성능 튜닝"),
                createNodeRequest("Maven", 0, 7, "의존성 및 멀티모듈 관리"),
                createNodeRequest("Spring Security", 0, 8, "LDAP, 권한 시스템 설계"),
                createNodeRequest("JPA", 0, 9, "복잡한 도메인 모델링과 최적화"),
                createNodeRequest("JUnit", 0, 10, "통합/단위 테스트 전략")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "엔터프라이즈 자바 로드맵 (기초 포함)",
                "대규모 환경에서 요구되는 기술들.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createPythonDjangoRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor4@test.com", "멘토4", "test4", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "Python 기초: 자료형/함수/파일 입출력 등 (권장 실습: 간단 스크립트)"),
                createNodeRequest("Git", 0, 2, "버전관리 및 협업 실습"),
                createNodeRequest("Python", 0, 3, "파이썬 고급 문법: 데코레이터/컨텍스트 매니저"),
                createNodeRequest("HTTP", 0, 4, "웹 기초 및 요청/응답 이해"),
                createNodeRequest("Django", 0, 5, "MVT 패턴 및 ORM 사용"),
                createNodeRequest("PostgreSQL", 0, 6, "마이그레이션과 고급 쿼리"),
                createNodeRequest("Redis", 0, 7, "캐싱 및 비동기 작업과 연계"),
                createNodeRequest("Docker", 0, 8, "컨테이너로 배포 연습 (간단)")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "파이썬 Django 백엔드 로드맵 (기초 포함)",
                "빠른 개발/프로토타입 중심 로드맵.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createNodeJSRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor5@test.com", "멘토5", "test5", "1234", "백엔드 개발자", 2);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "언어 기초 점검 (JS: 자료형/비동기)"),
                createNodeRequest("Git", 0, 2, "협업 워크플로 숙지"),
                createNodeRequest("JavaScript", 0, 3, "ES6+ 및 비동기 패턴"),
                createNodeRequest("Node.js", 0, 4, "이벤트 루프 및 서버 구현"),
                createNodeRequest("Express.js", 0, 5, "라우팅/미들웨어 패턴 실습"),
                createNodeRequest("MongoDB", 0, 6, "문서형 DB 기초 및 CRUD 실습"),
                createNodeRequest("Docker", 0, 7, "컨테이너화 및 배포 기본"),
                createNodeRequest("AWS", 0, 8, "간단한 서버 배포 실습 (Lambda/EC2)")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "Node.js 백엔드 로드맵 (기초 포함)",
                "빠른 프로토타입과 실시간 기능 중심 로드맵.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createFullStackRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor6@test.com", "멘토6", "test6", "1234", "백엔드 개발자", 6);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검 및 작은 실습"),
                createNodeRequest("Git", 0, 2, "협업"),
                createNodeRequest("Java", 0, 3, "언어 기초 및 객체지향"),
                createNodeRequest("JavaScript", 0, 4, "프론트 기본"),
                createNodeRequest("Spring Boot", 0, 5, "REST API 구현"),
                createNodeRequest("MySQL", 0, 6, "DB 기초 설계"),
                createNodeRequest("React", 0, 7, "기본 컴포넌트와 API 연동 실습"),
                createNodeRequest("JPA", 0, 8, "ORM 실습"),
                createNodeRequest("Docker", 0, 9, "컨테이너화"),
                createNodeRequest("Kubernetes", 0, 10, "오케스트레이션 기초")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "풀스택 백엔드 로드맵 (기초 포함)",
                "백엔드 중심이지만 프론트 연계 역량 포함.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createDevOpsRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("mentor7@test.com", "멘토7", "test7", "1234", "백엔드 개발자", 7);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 문법 복습 및 스크립트 실습"),
                createNodeRequest("Linux", 0, 2, "서버 운영 기초"),
                createNodeRequest("Git", 0, 3, "GitOps와 CI 프로세스 실습"),
                createNodeRequest("Docker", 0, 4, "컨테이너화 및 이미지 관리"),
                createNodeRequest("Kubernetes", 0, 5, "클러스터 기본 구성 및 배포"),
                createNodeRequest("AWS", 0, 6, "인프라 구성 및 비용 고려"),
                createNodeRequest("Jenkins", 0, 7, "CI/CD 파이프라인 구성"),
                createNodeRequest("Monitoring", 0, 8, "서비스 모니터링 및 알람 설정"),
                createNodeRequest("Nginx", 0, 9, "리버스 프록시/로드밸런싱")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "DevOps 포함 백엔드 로드맵 (기초 포함)",
                "개발-배포-운영까지 아우르는 로드맵.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    // ---------------------- 보강 샘플들 (추가) ----------------------

    private void createJuniorBackendRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("junior1@test.com", "주니어멘토1", "junior1", "1234", "백엔드 개발자", 1);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "변수/조건문/반복문/함수 등 기초. 권장 실습: '간단 계산기' 또는 '문자열 통계' 콘솔 앱 (30~60분)."),
                createNodeRequest("Terminal / CLI", 0, 2, "터미널 파일/디렉토리 조작, 간단 쉘명령 사용 연습."),
                createNodeRequest("Git", 0, 3, "로컬 repo 생성 · commit · branch · push · PR 실습. 권장 실습: GitHub에 프로젝트 올리기."),
                createNodeRequest("HTML/CSS", 0, 4, "간단한 웹 페이지 구성 실습(폼 만들기)"),
                createNodeRequest("HTTP", 0, 5, "요청/응답과 상태 코드 실습 (curl/postman 사용 권장)"),
                createNodeRequest("Java", 0, 6, "언어 기초: OOP와 간단한 콘솔 앱 제작"),
                createNodeRequest("Mini: Todo REST API (설계+구현)", 0, 7,
                        "권장 실습 프로젝트: 간단한 Todo REST API 제작. 요구사항: CRUD, 간단한 유저 식별(토큰 X), DB는 로컬 MySQL 또는 H2 사용. (목표: 2~4시간 작업)"),
                createNodeRequest("MySQL", 0, 8, "기본 쿼리, 스키마 설계 기초")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "주니어 백엔드 입문 로드맵 (기초+실습)",
                "고등학생·비전공자 대상: 개념 → 터미널·버전관리 → 작은 프로젝트 → DB 기초 → 프레임워크 진입 권장",
                nodes
        );
        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createApiFirstRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("apifirst@test.com", "API멘토", "apimentor", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검"),
                createNodeRequest("Git", 0, 2, "협업 워크플로"),
                createNodeRequest("HTTP", 0, 3, "프로토콜 기초와 헤더/상태 코드 이해"),
                createNodeRequest("REST API", 0, 4, "리소스 설계와 엔드포인트 설계 원칙"),
                createNodeRequest("Postman", 0, 5, "API 테스트 및 문서화"),
                createNodeRequest("GraphQL", 0, 6, "옵션: 데이터 페칭 패턴 이해"),
                createNodeRequest("Security Basics", 0, 7, "인증/인가 개념, CORS 등")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "API-First 설계 로드맵",
                "API 설계 중심 개발자 로드맵 (기초 포함)",
                nodes
        );
        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createDataFocusedRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("data1@test.com", "데이터멘토", "data1", "1234", "백엔드 개발자", 5);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검"),
                createNodeRequest("Data Modeling", 0, 2, "스키마 설계: 정규화/비정규화"),
                createNodeRequest("SQL", 0, 3, "집계, JOIN, 서브쿼리"),
                createNodeRequest("PostgreSQL", 0, 4, "고급 쿼리/JSONB"),
                createNodeRequest("Redis", 0, 5, "캐싱 전략"),
                createNodeRequest("Kafka", 0, 6, "스트리밍 패턴 기초")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "데이터 중심 백엔드 로드맵",
                "데이터 설계/처리 중심 로드맵 (기초 포함)",
                nodes
        );
        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createSecurityFocusedRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("security@test.com", "시큐리티멘토", "sec1", "1234", "백엔드 개발자", 6);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검"),
                createNodeRequest("Security Basics", 0, 2, "취약점/보안 원칙 이해"),
                createNodeRequest("Spring Security", 0, 3, "인증/인가 구현"),
                createNodeRequest("JWT", 0, 4, "토큰 기반 인증 패턴"),
                createNodeRequest("OAuth2", 0, 5, "권한 위임/소셜 로그인"),
                createNodeRequest("HTTPS", 0, 6, "TLS/SSL 개념 및 배포 설정")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "보안 중심 백엔드 로드맵",
                "보안 중심 로드맵 (기초 포함)",
                nodes
        );
        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createTestingFocusedRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("testmentor@test.com", "테스트멘토", "testm", "1234", "백엔드 개발자", 5);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검"),
                createNodeRequest("TDD", 0, 2, "테스트 주도 개발 실습"),
                createNodeRequest("JUnit", 0, 3, "단위 테스트"),
                createNodeRequest("Mockito", 0, 4, "목 기반 단위 테스트"),
                createNodeRequest("CI/CD", 0, 5, "테스트 자동화 파이프라인"),
                createNodeRequest("Monitoring", 0, 6, "테스트/운영 모니터링 연계")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 중심 백엔드 로드맵",
                "테스트 자동화 및 품질 관점 로드맵 (기초 포함)",
                nodes
        );
        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createFrontendIntegrationRoadmap(Job backendJob) {
        Member member = memberService.joinMentor("feintegr@test.com", "FE연계멘토", "fe1", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "기초 점검"),
                createNodeRequest("Git", 0, 2, "협업"),
                createNodeRequest("JavaScript", 0, 3, "프론트 통신 기초"),
                createNodeRequest("REST API", 0, 4, "프론트 연동 API 설계"),
                createNodeRequest("React", 0, 5, "컴포넌트 및 상태관리 실습"),
                createNodeRequest("CORS & Security Basics", 0, 6, "프론트-백엔드 연동 시 보안 이슈 처리 실습")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "프론트엔드 연계 백엔드 로드맵",
                "프론트와 협업하는 백엔드 개발자 관점 로드맵 (기초 포함)",
                nodes
        );
        mentorRoadmapService.create(mentor.getId(), request);
    }

    // ---------------------- 헬퍼들 ----------------------

    private Mentor updateMentorJob(Member member, Job job) {
        Mentor mentor = mentorRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다: " + member.getId()));

        Mentor updatedMentor = Mentor.builder()
                .member(mentor.getMember())
                .jobId(job.getId())
                .careerYears(mentor.getCareerYears())
                .rate(mentor.getRate())
                .build();

        mentorRepository.delete(mentor);
        return mentorRepository.save(updatedMentor);
    }

    /**
     * Task가 DB에 없으면 자동 생성하도록 안전장치 추가.
     * 프로젝트 제안은 description에 구체적으로 적음 (Task로 만들지 않음).
     */
    private RoadmapNodeRequest createNodeRequest(String taskName, int level, int stepOrder, String description) {
        Task task = taskRepository.findByNameIgnoreCase(taskName)
                .orElseGet(() -> taskService.create(taskName)); // 누락 시 자동 생성
        return new RoadmapNodeRequest(
                task != null ? task.getId() : null,
                taskName,
                description,
                stepOrder
        );
    }

    // --- 직업 로드맵 샘플 데이터 생성 (API 테스트용) ---
    public void initSampleJobRoadmap() {
        if (jobRoadmapRepository.count() > 0) return;

        Job backendJob = jobRepository.findByName("백엔드 개발자")
                .orElseThrow(() -> new RuntimeException("백엔드 개발자 직업을 찾을 수 없습니다."));

        Job frontendJob = jobRepository.findByName("프론트엔드 개발자")
                .orElseThrow(() -> new RuntimeException("프론트엔드 개발자 직업을 찾을 수 없습니다."));

        // 백엔드 개발자 직업 로드맵 생성 (트리 구조로 구성)
        JobRoadmap jobRoadmap = JobRoadmap.builder()
                .job(backendJob)
                .build();
        jobRoadmap = jobRoadmapRepository.save(jobRoadmap);

        // 다건 조회 확인용 프론트엔드 개발자 직업 로드맵 생성 (빈 로드맵)
        JobRoadmap frontendRoadmap = JobRoadmap.builder()
                .job(frontendJob)
                .build();

        // Task 조회 (이미 생성된 Task들 사용)
        Task programmingFundamentals = taskRepository.findByNameIgnoreCase("Programming Fundamentals").orElse(null);
        Task git = taskRepository.findByNameIgnoreCase("Git").orElse(null);
        Task java = taskRepository.findByNameIgnoreCase("Java").orElse(null);
        Task springBoot = taskRepository.findByNameIgnoreCase("Spring Boot").orElse(null);
        Task mysql = taskRepository.findByNameIgnoreCase("MySQL").orElse(null);
        Task jpa = taskRepository.findByNameIgnoreCase("JPA").orElse(null);
        Task docker = taskRepository.findByNameIgnoreCase("Docker").orElse(null);
        Task aws = taskRepository.findByNameIgnoreCase("AWS").orElse(null);

        // 트리 구조로 노드 생성 (루트 노드들과 자식 노드들)

        // 루트 노드 1: Programming Fundamentals (level=0, stepOrder=1)
        RoadmapNode fundamentalsNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(programmingFundamentals)
                .taskName("Programming Fundamentals")
                .description("프로그래밍의 기초 개념: 변수, 조건문, 반복문, 함수 등을 이해하고 활용할 수 있습니다.")
                .stepOrder(1)
                .level(0)
                .build();

        // 루트 노드 2: Git (level=0, stepOrder=2)
        RoadmapNode gitNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(git)
                .taskName("Git")
                .description("버전 관리 시스템으로 코드 히스토리 관리 및 협업을 위한 필수 도구입니다.")
                .stepOrder(2)
                .level(0)
                .build();

        // Fundamentals의 자식 노드들
        RoadmapNode javaNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(java)
                .taskName("Java")
                .description("객체지향 프로그래밍 언어로 백엔드 개발의 기초가 되는 언어입니다.")
                .stepOrder(1)
                .level(1)
                .build();

        RoadmapNode springBootNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(springBoot)
                .taskName("Spring Boot")
                .description("Java 기반의 웹 애플리케이션 프레임워크로 REST API 개발에 필수입니다.")
                .stepOrder(2)
                .level(1)
                .build();

        // Java의 자식 노드들
        RoadmapNode mysqlNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(mysql)
                .taskName("MySQL")
                .description("관계형 데이터베이스로 데이터 저장 및 관리를 위한 기본 기술입니다.")
                .stepOrder(1)
                .level(2)
                .build();

        RoadmapNode jpaNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(jpa)
                .taskName("JPA")
                .description("Java 진영의 ORM 기술로 객체와 관계형 데이터베이스를 매핑합니다.")
                .stepOrder(2)
                .level(2)
                .build();

        // Spring Boot의 자식 노드들
        RoadmapNode dockerNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(docker)
                .taskName("Docker")
                .description("컨테이너 기술로 애플리케이션 배포 및 환경 관리를 간소화합니다.")
                .stepOrder(1)
                .level(2)
                .build();

        RoadmapNode awsNode = RoadmapNode.builder()
                .roadmapId(jobRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(aws)
                .taskName("AWS")
                .description("클라우드 서비스로 애플리케이션을 확장 가능하게 배포하고 운영합니다.")
                .stepOrder(2)
                .level(2)
                .build();

        // 트리 구조 연결 (addChild 메서드 사용)
        fundamentalsNode.addChild(javaNode);
        fundamentalsNode.addChild(springBootNode);
        javaNode.addChild(mysqlNode);
        javaNode.addChild(jpaNode);
        springBootNode.addChild(dockerNode);
        springBootNode.addChild(awsNode);

        // 모든 노드를 JobRoadmap에 추가
        jobRoadmap.getNodes().addAll(List.of(
                fundamentalsNode, gitNode, javaNode, springBootNode,
                mysqlNode, jpaNode, dockerNode, awsNode
        ));

        jobRoadmapRepository.save(jobRoadmap);
        jobRoadmapRepository.save(frontendRoadmap); // 빈 로드맵 저장
    }

    // --- 통합 로직 테스트용 멘토 로드맵 10개 생성 ---
    public void initTestMentorRoadmaps() {
        if (mentorRoadmapRepository.count() > 0) {
            log.info("멘토 로드맵이 이미 존재합니다. 테스트 로드맵 생성을 건너뜁니다.");
            return;
        }

        Job backendJob = jobRepository.findByName("백엔드 개발자")
                .orElseThrow(() -> new RuntimeException("백엔드 개발자 직업을 찾을 수 없습니다."));

        log.info("=== 통합 테스트용 멘토 로드맵 10개 생성 시작 ===");

        // Java 경로 5개
        createTestJavaRoadmap1(backendJob);
        createTestJavaRoadmap2(backendJob);
        createTestJavaRoadmap3(backendJob);
        createTestJavaRoadmap4(backendJob);
        createTestJavaRoadmap5(backendJob);

        // JavaScript 경로 3개
        createTestJavaScriptRoadmap1(backendJob);
        createTestJavaScriptRoadmap2(backendJob);
        createTestJavaScriptRoadmap3(backendJob);

        // Python 경로 2개
        createTestPythonRoadmap1(backendJob);
        createTestPythonRoadmap2(backendJob);

        log.info("=== 통합 테스트용 멘토 로드맵 10개 생성 완료 ===");
    }

    // Java 경로 로드맵들
    private void createTestJavaRoadmap1(Job backendJob) {
        Member member = memberService.joinMentor("test.java1@test.com", "자바멘토1", "javamentor1", "1234", "백엔드 개발자", 3);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Java", 0, 4, "자바 언어 기초"),
                createNodeRequest("Spring", 0, 5, "스프링 프레임워크"),
                createNodeRequest("Spring Boot", 0, 6, "스프링 부트"),
                createNodeRequest("MySQL", 0, 7, "MySQL 데이터베이스"),
                createNodeRequest("JPA", 0, 8, "JPA ORM"),
                createNodeRequest("Docker", 0, 9, "도커 컨테이너"),
                createNodeRequest("AWS", 0, 10, "AWS 클라우드")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 자바 로드맵 1",
                "Java -> Spring -> Spring Boot -> MySQL -> JPA -> Docker -> AWS",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("자바 로드맵 1 생성 완료");
    }

    private void createTestJavaRoadmap2(Job backendJob) {
        Member member = memberService.joinMentor("test.java2@test.com", "자바멘토2", "javamentor2", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Java", 0, 4, "자바 언어 기초"),
                createNodeRequest("Spring", 0, 5, "스프링 프레임워크"),
                createNodeRequest("Spring Boot", 0, 6, "스프링 부트"),
                createNodeRequest("PostgreSQL", 0, 7, "PostgreSQL 데이터베이스"),
                createNodeRequest("Redis", 0, 8, "Redis 캐싱"),
                createNodeRequest("Kubernetes", 0, 9, "쿠버네티스"),
                createNodeRequest("Jenkins", 0, 10, "젠킨스 CI/CD")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 자바 로드맵 2",
                "Java -> Spring -> Spring Boot -> PostgreSQL -> Redis -> Kubernetes -> Jenkins",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("자바 로드맵 2 생성 완료");
    }

    private void createTestJavaRoadmap3(Job backendJob) {
        Member member = memberService.joinMentor("test.java3@test.com", "자바멘토3", "javamentor3", "1234", "백엔드 개발자", 5);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Java", 0, 4, "자바 언어 기초"),
                createNodeRequest("Spring", 0, 5, "스프링 프레임워크"),
                createNodeRequest("Spring Boot", 0, 6, "스프링 부트"),
                createNodeRequest("MySQL", 0, 7, "MySQL 데이터베이스"),
                createNodeRequest("Docker", 0, 8, "도커 컨테이너"),
                createNodeRequest("Monitoring", 0, 9, "모니터링"),
                createNodeRequest("Logging", 0, 10, "로깅")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 자바 로드맵 3",
                "Java -> Spring -> Spring Boot -> MySQL -> Docker -> Monitoring -> Logging",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("자바 로드맵 3 생성 완료");
    }

    private void createTestJavaRoadmap4(Job backendJob) {
        Member member = memberService.joinMentor("test.java4@test.com", "자바멘토4", "javamentor4", "1234", "백엔드 개발자", 6);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Java", 0, 4, "자바 언어 기초"),
                createNodeRequest("Spring", 0, 5, "스프링 프레임워크"),
                createNodeRequest("Spring Boot", 0, 6, "스프링 부트"),
                createNodeRequest("PostgreSQL", 0, 7, "PostgreSQL 데이터베이스"),
                createNodeRequest("JPA", 0, 8, "JPA ORM"),
                createNodeRequest("Spring Security", 0, 9, "스프링 시큐리티"),
                createNodeRequest("Testing", 0, 10, "테스팅")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 자바 로드맵 4",
                "Java -> Spring -> Spring Boot -> PostgreSQL -> JPA -> Spring Security -> Testing",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("자바 로드맵 4 생성 완료");
    }

    private void createTestJavaRoadmap5(Job backendJob) {
        Member member = memberService.joinMentor("test.java5@test.com", "자바멘토5", "javamentor5", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Java", 0, 4, "자바 언어 기초"),
                createNodeRequest("Spring", 0, 5, "스프링 프레임워크"),
                createNodeRequest("Spring Boot", 0, 6, "스프링 부트"),
                createNodeRequest("MySQL", 0, 7, "MySQL 데이터베이스"),
                createNodeRequest("Redis", 0, 8, "Redis 캐싱"),
                createNodeRequest("Docker", 0, 9, "도커 컨테이너"),
                createNodeRequest("CI/CD", 0, 10, "CI/CD 파이프라인")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 자바 로드맵 5",
                "Java -> Spring -> Spring Boot -> MySQL -> Redis -> Docker -> CI/CD",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("자바 로드맵 5 생성 완료");
    }

    // JavaScript 경로 로드맵들
    private void createTestJavaScriptRoadmap1(Job backendJob) {
        Member member = memberService.joinMentor("test.js1@test.com", "JS멘토1", "jsmentor1", "1234", "백엔드 개발자", 3);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("JavaScript", 0, 4, "자바스크립트 언어"),
                createNodeRequest("Node.js", 0, 5, "Node.js 런타임"),
                createNodeRequest("Express.js", 0, 6, "Express.js 프레임워크"),
                createNodeRequest("MongoDB", 0, 7, "MongoDB 데이터베이스"),
                createNodeRequest("Docker", 0, 8, "도커 컨테이너"),
                createNodeRequest("AWS", 0, 9, "AWS 클라우드"),
                createNodeRequest("Nginx", 0, 10, "Nginx 웹서버")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 JS 로드맵 1",
                "JavaScript -> Node.js -> Express -> MongoDB -> Docker -> AWS -> Nginx",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("JavaScript 로드맵 1 생성 완료");
    }

    private void createTestJavaScriptRoadmap2(Job backendJob) {
        Member member = memberService.joinMentor("test.js2@test.com", "JS멘토2", "jsmentor2", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("JavaScript", 0, 4, "자바스크립트 언어"),
                createNodeRequest("Node.js", 0, 5, "Node.js 런타임"),
                createNodeRequest("Express.js", 0, 6, "Express.js 프레임워크"),
                createNodeRequest("MongoDB", 0, 7, "MongoDB 데이터베이스"),
                createNodeRequest("Redis", 0, 8, "Redis 캐싱"),
                createNodeRequest("Docker", 0, 9, "도커 컨테이너"),
                createNodeRequest("Kubernetes", 0, 10, "쿠버네티스")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 JS 로드맵 2",
                "JavaScript -> Node.js -> Express -> MongoDB -> Redis -> Docker -> Kubernetes",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("JavaScript 로드맵 2 생성 완료");
    }

    private void createTestJavaScriptRoadmap3(Job backendJob) {
        Member member = memberService.joinMentor("test.js3@test.com", "JS멘토3", "jsmentor3", "1234", "백엔드 개발자", 2);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("JavaScript", 0, 4, "자바스크립트 언어"),
                createNodeRequest("Node.js", 0, 5, "Node.js 런타임"),
                createNodeRequest("Express.js", 0, 6, "Express.js 프레임워크"),
                createNodeRequest("PostgreSQL", 0, 7, "PostgreSQL 데이터베이스"),
                createNodeRequest("Testing", 0, 8, "테스팅"),
                createNodeRequest("Monitoring", 0, 9, "모니터링"),
                createNodeRequest("Logging", 0, 10, "로깅")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 JS 로드맵 3",
                "JavaScript -> Node.js -> Express -> PostgreSQL -> Testing -> Monitoring -> Logging",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("JavaScript 로드맵 3 생성 완료");
    }

    // Python 경로 로드맵들
    private void createTestPythonRoadmap1(Job backendJob) {
        Member member = memberService.joinMentor("test.py1@test.com", "파이썬멘토1", "pymentor1", "1234", "백엔드 개발자", 3);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Python", 0, 4, "파이썬 언어"),
                createNodeRequest("Django", 0, 5, "Django 프레임워크"),
                createNodeRequest("PostgreSQL", 0, 6, "PostgreSQL 데이터베이스"),
                createNodeRequest("Redis", 0, 7, "Redis 캐싱"),
                createNodeRequest("Docker", 0, 8, "도커 컨테이너"),
                createNodeRequest("AWS", 0, 9, "AWS 클라우드"),
                createNodeRequest("Monitoring", 0, 10, "모니터링")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 파이썬 로드맵 1",
                "Python -> Django -> PostgreSQL -> Redis -> Docker -> AWS -> Monitoring",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("Python 로드맵 1 생성 완료");
    }

    private void createTestPythonRoadmap2(Job backendJob) {
        Member member = memberService.joinMentor("test.py2@test.com", "파이썬멘토2", "pymentor2", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Programming Fundamentals", 0, 1, "프로그래밍 기초"),
                createNodeRequest("Git", 0, 2, "버전 관리"),
                createNodeRequest("HTTP", 0, 3, "HTTP 프로토콜"),
                createNodeRequest("Python", 0, 4, "파이썬 언어"),
                createNodeRequest("Django", 0, 5, "Django 프레임워크"),
                createNodeRequest("MySQL", 0, 6, "MySQL 데이터베이스"),
                createNodeRequest("Docker", 0, 7, "도커 컨테이너"),
                createNodeRequest("Kubernetes", 0, 8, "쿠버네티스"),
                createNodeRequest("Jenkins", 0, 9, "젠킨스 CI/CD"),
                createNodeRequest("Logging", 0, 10, "로깅")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "테스트 파이썬 로드맵 2",
                "Python -> Django -> MySQL -> Docker -> Kubernetes -> Jenkins -> Logging",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
        log.info("Python 로드맵 2 생성 완료");
    }

    // --- 통합 로직 테스트 및 트리 구조 출력 ---
    public void testJobRoadmapIntegration() {
        Job backendJob = jobRepository.findByName("백엔드 개발자")
                .orElseThrow(() -> new RuntimeException("백엔드 개발자 직업을 찾을 수 없습니다."));

        log.info("\n\n=== 직업 로드맵 통합 시작 ===");
        JobRoadmap integratedRoadmap = jobRoadmapIntegrationService.integrateJobRoadmap(backendJob.getId());
        log.info("=== 직업 로드맵 통합 완료 ===\n");

        log.info("\n\n=== 통합된 직업 로드맵 트리 구조 출력 ===");
        printJobRoadmapTree(integratedRoadmap);
        log.info("=== 트리 구조 출력 완료 ===\n\n");
    }

    // --- 트리 구조 출력 헬퍼 메서드 ---
    private void printJobRoadmapTree(JobRoadmap jobRoadmap) {
        log.info("직업 로드맵 ID: {}", jobRoadmap.getId());
        log.info("직업: {}", jobRoadmap.getJob().getName());
        log.info("총 노드 수: {}", jobRoadmap.getNodes().size());
        log.info("\n트리 구조:");

        // 루트 노드들 찾기 (parent가 null인 노드들)
        List<RoadmapNode> rootNodes = jobRoadmap.getNodes().stream()
                .filter(node -> node.getParent() == null)
                .sorted(Comparator.comparingInt(RoadmapNode::getStepOrder))
                .toList();

        log.info("루트 노드 수: {}", rootNodes.size());

        for (RoadmapNode rootNode : rootNodes) {
            printNodeRecursive(rootNode, "", true);
        }
    }

    private void printNodeRecursive(RoadmapNode node, String prefix, boolean isLast) {
        // 현재 노드 출력
        String connector = isLast ? "└── " : "├── ";
        String nodeInfo = String.format("%s (level=%d, stepOrder=%d, id=%d)",
                node.getTaskName(),
                node.getLevel(),
                node.getStepOrder(),
                node.getId());

        log.info("{}{}{}", prefix, connector, nodeInfo);

        // 자식 노드들 정렬 (stepOrder 기준)
        List<RoadmapNode> children = node.getChildren().stream()
                .sorted(Comparator.comparingInt(RoadmapNode::getStepOrder))
                .toList();

        // 자식 노드들 재귀 출력
        String childPrefix = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < children.size(); i++) {
            boolean isLastChild = (i == children.size() - 1);
            printNodeRecursive(children.get(i), childPrefix, isLastChild);
        }
    }
}
