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
import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapNodeStatRepository;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.roadmap.service.MentorRoadmapService;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.domain.roadmap.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Transactional
@Profile("prod")
public class RoadmapProdInitData {
    private final JobService jobService;
    private final JobRepository jobRepository;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final MemberService memberService;
    private final MentorRepository mentorRepository;
    private final MentorRoadmapService mentorRoadmapService;
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final JobRoadmapRepository jobRoadmapRepository;
    private final JobRoadmapNodeStatRepository jobRoadmapNodeStatRepository;


    @Bean
    ApplicationRunner baseInitDataApplicationRunner2() {
        return args -> runInitData();
    }

    @Transactional
    public void runInitData() {
        initJobData();
        initTaskData();           // 보강된 Task 목록
        //initSampleJobRoadmap();   // 직업 로드맵 조회 API 테스트용 샘플 데이터

        // 통합 로직 테스트
        //initSampleMentorRoadmaps();    // 샘플 멘토 로드맵 10개 생성

    }

    // --- Job 초기화 ---
    @Transactional
    public void initJobData() {
        if (jobService.count() > 0) return;

        // 백엔드 개발자
        Job job1 = jobService.create("백엔드 개발자", "서버 사이드 로직을 구현하고, 데이터베이스 및 API를 설계·운영하는 개발자입니다.");
        jobService.createAlias(job1, "백엔드");
        jobService.createAlias(job1, "BE 개발자");
        jobService.createAlias(job1, "Backend 개발자");
        jobService.createAlias(job1, "서버 개발자");
        jobService.createAlias(job1, "API 개발자");

        // 프론트엔드 개발자
        Job job2 = jobService.create("프론트엔드 개발자", "웹 또는 앱의 사용자 인터페이스(UI)와 사용자 경험(UX)을 담당하며, 사용자가 직접 보는 화면을 구현하는 개발자입니다.");
        jobService.createAlias(job2, "프론트엔드");
        jobService.createAlias(job2, "FE 개발자");
        jobService.createAlias(job2, "Frontend 개발자");
        jobService.createAlias(job2, "웹 퍼블리셔");
        jobService.createAlias(job2, "UI 개발자");
        jobService.createAlias(job2, "클라이언트 개발자");

        // 모바일 앱 개발자
        Job job3 = jobService.create(
                "모바일 앱 개발자",
                "스마트폰과 태블릿 환경에서 동작하는 iOS 또는 Android 애플리케이션을 개발하는 직군으로, 플랫폼별 네이티브 또는 크로스플랫폼 기술을 활용합니다."
        );

        // 데이터 엔지니어
        Job job4 = jobService.create(
                "데이터 엔지니어",
                "데이터를 수집·저장·처리할 수 있는 파이프라인을 설계하고 구축하는 전문가로, 데이터 분석과 AI 모델링의 기반을 마련합니다."
        );

        // 데이터 분석가
        Job job5 = jobService.create(
                "데이터 분석가",
                "데이터를 기반으로 비즈니스 인사이트를 도출하고, 통계 분석과 시각화를 통해 의사결정을 지원하는 직군입니다."
        );

        // AI / 머신러닝 엔지니어
        Job job6 = jobService.create(
                "AI/ML 엔지니어",
                "머신러닝과 딥러닝 알고리즘을 활용해 예측 모델과 인공지능 서비스를 개발하는 직군으로, 데이터 처리와 모델 학습에 대한 이해가 필요합니다."
        );

        // DevOps 엔지니어
        Job job7 = jobService.create(
                "DevOps 엔지니어",
                "개발(Development)과 운영(Operations)을 연결하여 CI/CD 파이프라인, 인프라 자동화, 배포 환경을 최적화하는 엔지니어입니다."
        );

        // 클라우드 엔지니어
        Job job8 = jobService.create(
                "클라우드 엔지니어",
                "AWS, GCP, Azure 등 클라우드 환경에서 인프라를 설계·배포·운영하며, 서비스의 안정성과 확장성을 책임지는 직군입니다."
        );

        // 사이버 보안 전문가
        Job job9 = jobService.create(
                "보안 엔지니어",
                "시스템과 네트워크의 보안 취약점을 점검하고, 공격 방어 및 보안 정책을 설계하는 역할을 수행합니다."
        );

        // 게임 서버/클라이언트 개발자
        Job job10 = jobService.create(
                "게임 개발자",
                "게임 클라이언트 또는 서버를 개발하는 직군으로, 그래픽·물리 엔진·네트워크 프로그래밍 등 다양한 기술을 다룹니다."
        );

        // QA / 테스트 엔지니어
        Job job11 = jobService.create(
                "QA 엔지니어",
                "소프트웨어 품질을 보증하기 위해 테스트를 설계·자동화·수행하는 직군으로, 버그 탐지와 품질 관리 프로세스를 담당합니다."
        );
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
        Task spring = taskService.create("Spring");
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

        // Frontend 관련
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
        taskService.createAlias(spring, "스프링");
        taskService.createAlias(springSecurity, "스프링 시큐리티");
        taskService.createAlias(programmingFundamentals, "프로그래밍 기초");
        taskService.createAlias(http, "HTTP 프로토콜");
    }



    // ---------------------- 헬퍼들 ----------------------

    private Mentor updateMentorJob(Member member, Job job) {
        Mentor mentor = mentorRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다: " + member.getId()));

        Mentor updatedMentor = Mentor.builder()
                .member(mentor.getMember())
                .job(job)
                .careerYears(mentor.getCareerYears())
                .rate(mentor.getRate())
                .build();

        mentorRepository.delete(mentor);
        return mentorRepository.save(updatedMentor);
    }

    /**
     * Task가 DB에 없으면 자동 생성하도록 안전장치 추가.
     * 통합 로직 테스트를 위해 모든 필드에 실제 값을 제공.
     */
    private RoadmapNodeRequest createNodeRequest(
            String taskName,
            int level,
            int stepOrder,
            String learningAdvice

    ) {
        Task task = taskRepository.findByNameIgnoreCase(taskName)
                .orElseGet(() -> taskService.create(taskName)); // 누락 시 자동 생성
        return new RoadmapNodeRequest(
                task != null ? task.getId() : null,
                taskName,
                learningAdvice,
                null,  // recommendedResources
                null,  // learningGoals
                null,  // difficulty
                null,  // importance
                null,  // hoursPerDay
                null,  // weeks
                stepOrder
        );
    }

    // --- 직업 로드맵 샘플 데이터 생성 (API 테스트용) ---
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initSampleJobRoadmap() {
        if (jobRoadmapRepository.count() > 0) return;

        Job backendJob = jobRepository.findByName("백엔드 개발자")
                .orElseThrow(() -> new RuntimeException("백엔드 개발자 직업을 찾을 수 없습니다."));

        Job frontendJob = jobRepository.findByName("프론트엔드 개발자")
                .orElseThrow(() -> new RuntimeException("프론트엔드 개발자 직업을 찾을 수 없습니다."));

        Job mobileJob = jobRepository.findByName("모바일 앱 개발자")
                .orElseThrow(() -> new RuntimeException("모바일 개발자 직업을 찾을 수 없습니다."));

        Job dataJob = jobRepository.findByName("데이터 엔지니어")
                .orElseThrow(() -> new RuntimeException("데이터 엔지니어 직업을 찾을 수 없습니다."));

        Job aiJob = jobRepository.findByName("AI/ML 엔지니어")
                .orElseThrow(() -> new RuntimeException("AI 엔지니어 직업을 찾을 수 없습니다."));

        // 조회용 멘토 및 직업 설정
        //Member member = memberService.joinMentor("testmentor@test.com", "멘토", "mentor", "1234", "백엔드 개발자", 6);
        //Mentor mentor = updateMentorJob(member, backendJob);

        // 백엔드 개발자 직업 로드맵 생성 (트리 구조로 구성)
        JobRoadmap backendRoadmap = JobRoadmap.builder()
                .job(backendJob)
                .build();
        backendRoadmap = jobRoadmapRepository.save(backendRoadmap);

        // 다른 직업 로드맵 생성 (간단한 버전)
        //createSimpleFrontendRoadmap(frontendJob);
        //createSimpleMobileRoadmap(mobileJob);
        //createSimpleDataEngineerRoadmap(dataJob);
        //createSimpleAiMlRoadmap(aiJob);

        // 다건 조회 확인용 다른 직업 로드맵 생성 (빈 로드맵)
        JobRoadmap frontendRoadmap = JobRoadmap.builder()
                .job(frontendJob)
                .build();
        jobRoadmapRepository.save(frontendRoadmap);

        JobRoadmap mobileRoadmap = JobRoadmap.builder()
                .job(mobileJob)
                .build();
        jobRoadmapRepository.save(mobileRoadmap);

        JobRoadmap dataRoadmap = JobRoadmap.builder()
                .job(dataJob)
                .build();
        jobRoadmapRepository.save(dataRoadmap);

        JobRoadmap aiRoadmap = JobRoadmap.builder()
                .job(aiJob)
                .build();
        jobRoadmapRepository.save(aiRoadmap);

        // Task 조회 (이미 생성된 Task들 사용)
        Task programmingFundamentals = taskRepository.findByNameIgnoreCase("Programming Fundamentals").orElse(null);
        Task java = taskRepository.findByNameIgnoreCase("Java").orElse(null);
        Task git = taskRepository.findByNameIgnoreCase("Git").orElse(null);
        Task nodejs = taskRepository.findByNameIgnoreCase("Node.js").orElse(null);
        Task python = taskRepository.findByNameIgnoreCase("Python").orElse(null);
        Task springBoot = taskRepository.findByNameIgnoreCase("Spring Boot").orElse(null);
        Task expressjs = taskRepository.findByNameIgnoreCase("Express.js").orElse(null);
        Task django = taskRepository.findByNameIgnoreCase("Django").orElse(null);
        Task jpa = taskRepository.findByNameIgnoreCase("JPA").orElse(null);
        Task mysql = taskRepository.findByNameIgnoreCase("MySQL").orElse(null);
        Task postgresql = taskRepository.findByNameIgnoreCase("PostgreSQL").orElse(null);
        Task http = taskRepository.findByNameIgnoreCase("HTTP").orElse(null);
        Task restApi = taskRepository.findByNameIgnoreCase("REST API").orElse(null);
        Task springSecurity = taskRepository.findByNameIgnoreCase("Spring Security").orElse(null);
        Task testing = taskRepository.findByNameIgnoreCase("Testing").orElse(null);
        Task ciCd = taskRepository.findByNameIgnoreCase("CI/CD").orElse(null);
        Task docker = taskRepository.findByNameIgnoreCase("Docker").orElse(null);
        Task caching = taskRepository.findByNameIgnoreCase("Caching").orElse(null);

        // ===== Level 0: 루트 노드 =====
        RoadmapNode fundamentalsNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(programmingFundamentals)
                .taskName("Programming Fundamentals")
                .learningAdvice("프로그래밍의 기초 개념(변수, 조건문, 반복문, 함수 등)을 이해하고, 간단한 알고리즘 문제를 해결할 수 있는 능력을 키웁니다.")
                .recommendedResources("생활코딩, 코드잇 프로그래밍 입문 강의, 프로그래머스 Lv.0~1 문제")
                .learningGoals("기본 자료구조 이해, 간단한 알고리즘 문제 풀이, 함수형 프로그래밍 개념 습득")
                .difficulty(1)
                .importance(5)
                .hoursPerDay(2)
                .weeks(4)
                .estimatedHours(2 * 4 * 7)  // hoursPerDay * weeks * 7
                .stepOrder(1)
                .level(0)
                .build();

        // ===== Level 1: 언어 선택 (Java, Git, Node.js, Python) =====
        RoadmapNode javaNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(java)
                .taskName("Java")
                .learningAdvice("객체지향 프로그래밍의 핵심 개념을 익히고, Java의 컬렉션 프레임워크와 스트림 API를 활용합니다.")
                .recommendedResources("Java의 정석, Effective Java, 백기선 자바 스터디")
                .learningGoals("OOP 4대 원칙 이해, 컬렉션/스트림 API 활용, 예외 처리 및 멀티스레딩 기초")
                .difficulty(3)
                .importance(5)
                .hoursPerDay(3)
                .weeks(6)
                .estimatedHours(3 * 6 * 7)
                .stepOrder(1)
                .level(1)
                .build();

        RoadmapNode gitNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(git)
                .taskName("Git")
                .learningAdvice("버전 관리 시스템의 핵심 개념을 이해하고, 브랜치 전략과 협업 워크플로를 익힙니다.")
                .recommendedResources("Pro Git(무료 e-book), Learn Git Branching, GitHub 공식 문서")
                .learningGoals("기본 명령어 숙달, 브랜치 전략 이해, 충돌 해결 능력, 협업 워크플로 실습")
                .difficulty(2)
                .importance(5)
                .hoursPerDay(1)
                .weeks(2)
                .estimatedHours(1 * 2 * 7)
                .stepOrder(2)
                .level(1)
                .build();

        RoadmapNode nodejsNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(nodejs)
                .taskName("Node.js")
                .learningAdvice("JavaScript 기반의 비동기 I/O 환경에서 이벤트 루프와 콜백 패턴을 이해합니다.")
                .recommendedResources("Node.js 공식 문서, The Node.js Handbook")
                .learningGoals("비동기 프로그래밍 이해, 이벤트 루프 동작 원리, NPM 패키지 관리")
                .difficulty(3)
                .importance(4)
                .hoursPerDay(2)
                .weeks(4)
                .estimatedHours(2 * 4 * 7)
                .stepOrder(3)
                .level(1)
                .build();

        RoadmapNode pythonNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(python)
                .taskName("Python")
                .learningAdvice("간결하고 읽기 쉬운 문법으로 빠른 개발이 가능하며, 데이터 처리에 강점이 있습니다.")
                .recommendedResources("점프 투 파이썬, Python 공식 튜토리얼")
                .learningGoals("파이썬 문법 완성, 리스트 컴프리헨션/제네레이터 이해, 데코레이터 활용")
                .difficulty(2)
                .importance(4)
                .hoursPerDay(2)
                .weeks(3)
                .estimatedHours(2 * 3 * 7)
                .stepOrder(4)
                .level(1)
                .build();

        // ===== Level 2: 프레임워크 선택 =====
        RoadmapNode springBootNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(springBoot)
                .taskName("Spring Boot")
                .learningAdvice("Java 생태계의 표준 프레임워크로, 의존성 주입과 관점 지향 프로그래밍 개념을 익힙니다.")
                .recommendedResources("스프링 부트와 AWS로 혼자 구현하는 웹 서비스, 백기선 스프링 부트 강의")
                .learningGoals("DI/IoC 개념 이해, REST API 구현, Actuator를 통한 모니터링")
                .difficulty(4)
                .importance(5)
                .hoursPerDay(3)
                .weeks(8)
                .estimatedHours(3 * 8 * 7)
                .stepOrder(1)
                .level(2)
                .build();

        RoadmapNode expressjsNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(expressjs)
                .taskName("Express.js")
                .learningAdvice("Node.js의 대표 웹 프레임워크로, 미들웨어 패턴과 라우팅을 학습합니다.")
                .recommendedResources("Express 공식 문서, Node.js 디자인 패턴 바이블")
                .learningGoals("미들웨어 체인 이해, RESTful API 설계, 에러 핸들링 전략")
                .difficulty(3)
                .importance(4)
                .hoursPerDay(2)
                .weeks(3)
                .estimatedHours(2 * 3 * 7)
                .stepOrder(1)
                .level(2)
                .build();

        RoadmapNode djangoNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(django)
                .taskName("Django")
                .learningAdvice("Python 기반의 풀스택 프레임워크로, MVT 패턴과 강력한 ORM을 제공합니다.")
                .recommendedResources("Django 공식 튜토리얼, Two Scoops of Django")
                .learningGoals("MVT 아키텍처 이해, Django ORM 활용, Admin 페이지 커스터마이징")
                .difficulty(3)
                .importance(4)
                .hoursPerDay(2)
                .weeks(4)
                .estimatedHours(2 * 4 * 7)
                .stepOrder(1)
                .level(2)
                .build();

        // ===== Level 3: Spring Boot 경로 - DB/ORM 및 HTTP =====
        RoadmapNode jpaNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(jpa)
                .taskName("JPA")
                .learningAdvice("객체와 관계형 데이터베이스를 매핑하는 ORM 기술로, 엔티티 설계와 연관관계 관리를 학습합니다.")
                .recommendedResources("자바 ORM 표준 JPA 프로그래밍(김영한), Hibernate 공식 문서")
                .learningGoals("엔티티 매핑, 연관관계 관리, 지연 로딩/즉시 로딩, 영속성 컨텍스트 이해")
                .difficulty(4)
                .importance(5)
                .hoursPerDay(3)
                .weeks(5)
                .estimatedHours(3 * 5 * 7)
                .stepOrder(1)
                .level(3)
                .build();

        RoadmapNode mysqlNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(mysql)
                .taskName("MySQL")
                .learningAdvice("가장 널리 사용되는 오픈소스 관계형 데이터베이스로, SQL 기본 문법과 인덱싱을 익힙니다.")
                .recommendedResources("Real MySQL, SQL 첫걸음")
                .learningGoals("CRUD 쿼리 작성, 인덱스 최적화, 트랜잭션 격리 수준 이해")
                .difficulty(3)
                .importance(5)
                .hoursPerDay(2)
                .weeks(4)
                .estimatedHours(2 * 4 * 7)
                .stepOrder(2)
                .level(3)
                .build();

        RoadmapNode postgresqlNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(postgresql)
                .taskName("PostgreSQL")
                .learningAdvice("표준 SQL 준수와 확장성이 뛰어난 오픈소스 RDBMS로, JSONB 등 고급 기능을 지원합니다.")
                .recommendedResources("PostgreSQL 공식 문서, The Art of PostgreSQL")
                .learningGoals("고급 쿼리 작성, JSONB 활용, 파티셔닝")
                .difficulty(4)
                .importance(4)
                .hoursPerDay(2)
                .weeks(3)
                .estimatedHours(2 * 3 * 7)
                .stepOrder(3)
                .level(3)
                .build();

        RoadmapNode httpNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(http)
                .taskName("HTTP")
                .learningAdvice("웹의 기반 프로토콜로, 요청/응답 구조, 메서드, 상태 코드, 헤더 등의 개념을 이해합니다.")
                .recommendedResources("HTTP 완벽 가이드, MDN Web Docs")
                .learningGoals("HTTP 메서드 이해, 상태 코드 활용, 헤더/쿠키 메커니즘")
                .difficulty(2)
                .importance(5)
                .hoursPerDay(2)
                .weeks(2)
                .estimatedHours(2 * 2 * 7)
                .stepOrder(4)
                .level(3)
                .build();

        // ===== Level 4: REST API =====
        RoadmapNode restApiNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(restApi)
                .taskName("REST API")
                .learningAdvice("RESTful 설계 원칙에 따라 리소스 기반 API를 설계하고, 적절한 HTTP 메서드와 상태 코드를 활용합니다.")
                .recommendedResources("RESTful Web API Patterns and Practices, Swagger/OpenAPI 문서")
                .learningGoals("REST 제약 조건 이해, 리소스 URI 설계, API 버전 관리")
                .difficulty(3)
                .importance(5)
                .hoursPerDay(2)
                .weeks(3)
                .estimatedHours(2 * 3 * 7)
                .stepOrder(1)
                .level(4)
                .build();

        // ===== Level 5: Spring Security =====
        RoadmapNode springSecurityNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(springSecurity)
                .taskName("Spring Security")
                .learningAdvice("인증과 인가를 구현하고, JWT, OAuth2 등의 보안 패턴을 학습합니다.")
                .recommendedResources("스프링 시큐리티 인 액션, Baeldung Spring Security 튜토리얼")
                .learningGoals("인증/인가 메커니즘 이해, JWT 토큰 기반 인증 구현, OAuth2/OIDC 통합")
                .difficulty(4)
                .importance(5)
                .hoursPerDay(3)
                .weeks(4)
                .estimatedHours(3 * 4 * 7)
                .stepOrder(1)
                .level(5)
                .build();

        // ===== Level 6: Testing =====
        RoadmapNode testingNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(testing)
                .taskName("Testing")
                .learningAdvice("단위 테스트, 통합 테스트, E2E 테스트를 작성하고, TDD 방법론을 실천합니다.")
                .recommendedResources("Test Driven Development(켄트 벡), JUnit 5 User Guide")
                .learningGoals("JUnit/Mockito 활용, 테스트 더블 패턴, 통합 테스트 전략")
                .difficulty(4)
                .importance(5)
                .hoursPerDay(2)
                .weeks(4)
                .estimatedHours(2 * 4 * 7)
                .stepOrder(1)
                .level(6)
                .build();

        // ===== Level 7: CI/CD, Docker, Caching =====
        RoadmapNode ciCdNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(ciCd)
                .taskName("CI/CD")
                .learningAdvice("지속적 통합과 지속적 배포 파이프라인을 구축하고, 자동화된 테스트와 배포 프로세스를 익힙니다.")
                .recommendedResources("The DevOps Handbook, GitHub Actions 공식 문서")
                .learningGoals("CI/CD 파이프라인 구축, 자동화된 테스트/빌드/배포, 롤백 전략")
                .difficulty(4)
                .importance(4)
                .hoursPerDay(2)
                .weeks(3)
                .estimatedHours(2 * 3 * 7)
                .stepOrder(1)
                .level(7)
                .build();

        RoadmapNode dockerNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(docker)
                .taskName("Docker")
                .learningAdvice("컨테이너 기술을 활용하여 일관된 개발 환경을 구성하고, 이미지 빌드와 배포를 자동화합니다.")
                .recommendedResources("Docker 공식 문서, Docker Deep Dive")
                .learningGoals("Dockerfile 작성, 이미지 빌드 최적화, Docker Compose 활용")
                .difficulty(3)
                .importance(5)
                .hoursPerDay(2)
                .weeks(3)
                .estimatedHours(2 * 3 * 7)
                .stepOrder(2)
                .level(7)
                .build();

        RoadmapNode cachingNode = RoadmapNode.builder()
                .roadmapId(backendRoadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(caching)
                .taskName("Caching")
                .learningAdvice("Redis, Memcached 등을 활용한 캐싱 전략으로 응답 속도를 개선하고 DB 부하를 줄입니다.")
                .recommendedResources("Redis 공식 문서, Caching Strategies and How to Choose the Right One")
                .learningGoals("캐싱 전략 이해, Redis 활용, 캐시 무효화 전략")
                .difficulty(4)
                .importance(4)
                .hoursPerDay(2)
                .weeks(2)
                .estimatedHours(2 * 2 * 7)
                .stepOrder(3)
                .level(7)
                .build();

        // ===== 트리 구조 연결 =====
        // Level 0 -> Level 1
        fundamentalsNode.addChild(javaNode);
        fundamentalsNode.addChild(gitNode);
        fundamentalsNode.addChild(nodejsNode);
        fundamentalsNode.addChild(pythonNode);

        // Level 1 -> Level 2
        javaNode.addChild(springBootNode);
        nodejsNode.addChild(expressjsNode);
        pythonNode.addChild(djangoNode);

        // Level 2 (Spring Boot) -> Level 3
        springBootNode.addChild(jpaNode);
        springBootNode.addChild(mysqlNode);
        springBootNode.addChild(postgresqlNode);
        springBootNode.addChild(httpNode);

        // Level 3 (HTTP) -> Level 4
        httpNode.addChild(restApiNode);

        // Level 4 (REST API) -> Level 5
        restApiNode.addChild(springSecurityNode);

        // Level 5 (Spring Security) -> Level 6
        springSecurityNode.addChild(testingNode);

        // Level 6 (Testing) -> Level 7
        testingNode.addChild(ciCdNode);
        testingNode.addChild(dockerNode);
        testingNode.addChild(cachingNode);

        // 모든 노드를 JobRoadmap에 추가
        backendRoadmap.getNodes().addAll(List.of(
                fundamentalsNode, javaNode, gitNode, nodejsNode, pythonNode,
                springBootNode, expressjsNode, djangoNode,
                jpaNode, mysqlNode, postgresqlNode, httpNode,
                restApiNode,
                springSecurityNode,
                testingNode,
                ciCdNode, dockerNode, cachingNode
        ));

        // JobRoadmap 저장 (cascade로 RoadmapNode들도 함께 저장됨)
        JobRoadmap savedJobRoadmap = jobRoadmapRepository.save(backendRoadmap);
        jobRoadmapRepository.flush(); // MySQL + auto-commit:false 환경을 위한 명시적 flush

        // ===== JobRoadmapNodeStat 샘플 데이터 생성 =====
        // 저장된 노드들을 다시 조회하여 영속화된 객체 사용
        JobRoadmap reloadedJobRoadmap = jobRoadmapRepository.findByIdWithJobAndNodes(savedJobRoadmap.getId())
                .orElseThrow(() -> new RuntimeException("저장된 JobRoadmap을 찾을 수 없습니다."));

        // 저장된 노드들을 taskName으로 매핑 (영속화된 노드 사용)
        java.util.Map<String, RoadmapNode> nodeMap = reloadedJobRoadmap.getNodes().stream()
                .collect(Collectors.toMap(RoadmapNode::getTaskName, node -> node));

        // totalMentorCount = 10 (가상의 멘토 10명 기준)
        int totalMentorCount = 10;

        // Level 0: Root (모든 멘토가 시작)
        createNodeStat(nodeMap.get("Programming Fundamentals"), 1, 1.0, 1.0, 10, totalMentorCount);

        // Level 1: 언어 선택
        createNodeStat(nodeMap.get("Java"), 1, 0.9, 1.5, 9, totalMentorCount); // Java 경로가 주류
        createNodeStat(nodeMap.get("Git"), 2, 0.95, 1.8, 10, totalMentorCount); // Git은 필수 도구
        createNodeStat(nodeMap.get("Node.js"), 3, 0.3, 2.2, 3, totalMentorCount); // Node.js는 소수
        createNodeStat(nodeMap.get("Python"), 4, 0.2, 2.5, 2, totalMentorCount); // Python은 더 소수

        // Level 2: 프레임워크
        createNodeStat(nodeMap.get("Spring Boot"), 1, 0.85, 2.8, 9, totalMentorCount); // Spring Boot 주류
        createNodeStat(nodeMap.get("Express.js"), 1, 0.28, 3.2, 3, totalMentorCount); // Express.js 소수
        createNodeStat(nodeMap.get("Django"), 1, 0.18, 3.5, 2, totalMentorCount); // Django 소수

        // Level 3: DB/ORM/HTTP
        createNodeStat(nodeMap.get("JPA"), 1, 0.65, 3.8, 7, totalMentorCount); // JPA는 Spring Boot 사용자의 대부분
        createNodeStat(nodeMap.get("MySQL"), 2, 0.68, 4.2, 7, totalMentorCount); // MySQL 인기
        createNodeStat(nodeMap.get("PostgreSQL"), 3, 0.48, 4.5, 5, totalMentorCount); // PostgreSQL은 중간
        createNodeStat(nodeMap.get("HTTP"), 4, 0.75, 4.0, 10, totalMentorCount); // HTTP는 모두 학습

        // Level 4: REST API
        createNodeStat(nodeMap.get("REST API"), 1, 0.78, 5.2, 9, totalMentorCount); // REST API는 거의 필수

        // Level 5: Spring Security
        createNodeStat(nodeMap.get("Spring Security"), 1, 0.62, 6.0, 7, totalMentorCount); // 보안은 중요하지만 진입 장벽

        // Level 6: Testing
        createNodeStat(nodeMap.get("Testing"), 1, 0.7, 6.8, 8, totalMentorCount); // 테스팅은 대부분 학습

        // Level 7: CI/CD, Docker, Caching
        createNodeStat(nodeMap.get("CI/CD"), 1, 0.52, 7.5, 6, totalMentorCount); // CI/CD는 중간 수준
        createNodeStat(nodeMap.get("Docker"), 2, 0.72, 7.2, 8, totalMentorCount); // Docker는 인기
        createNodeStat(nodeMap.get("Caching"), 3, 0.45, 7.8, 5, totalMentorCount); // Caching은 고급 주제

        log.info("백엔드 개발자 직업 로드맵 샘플 데이터 생성 완료 (노드 18개, 통계 18개)");
    }

    /**
     * JobRoadmapNodeStat 샘플 데이터 생성 헬퍼 메서드
     *
     * @param node RoadmapNode
     * @param stepOrder 노드의 stepOrder
     * @param weight 가중치 (0.0 ~ 1.0)
     * @param averagePosition 평균 등장 위치 (1.0부터 시작)
     * @param mentorCount 사용한 멘토 수
     * @param totalMentorCount 전체 멘토 수
     */
    private void createNodeStat(RoadmapNode node, int stepOrder, double weight,
                                double averagePosition, int mentorCount, int totalMentorCount) {
        double mentorCoverageRatio = (double) mentorCount / totalMentorCount;

        JobRoadmapNodeStat stat = JobRoadmapNodeStat.builder()
                .node(node)
                .stepOrder(stepOrder)
                .weight(weight)
                .averagePosition(averagePosition)
                .mentorCount(mentorCount)
                .totalMentorCount(totalMentorCount)
                .mentorCoverageRatio(mentorCoverageRatio)
                .outgoingTransitions(mentorCount) // 간단히 mentorCount와 동일하게 설정
                .incomingTransitions(mentorCount) // 간단히 mentorCount와 동일하게 설정
                .transitionCounts(null) // 샘플 데이터에서는 null
                .alternativeParents(null) // 샘플 데이터에서는 null
                .build();

        jobRoadmapNodeStatRepository.save(stat);
    }

    // --- 통합 로직 테스트용 멘토 로드맵 10개 생성 ---
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initSampleMentorRoadmaps() {
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

        // MySQL + auto-commit:false 환경을 위한 명시적 flush
        mentorRoadmapRepository.flush();

        log.info("=== 통합 테스트용 멘토 로드맵 10개 생성 완료 (flush 포함) ===");
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

    // --- 간단한 직업 로드맵 생성 메서드들 ---
    /**
     * 프론트엔드 개발자 로드맵 생성
     * Programming Fundamentals -> Git -> HTML/CSS, React
     */
    private void createSimpleFrontendRoadmap(Job frontendJob) {
        JobRoadmap roadmap = JobRoadmap.builder()
                .job(frontendJob)
                .build();
        roadmap = jobRoadmapRepository.save(roadmap);

        Task fundamentals = taskRepository.findByNameIgnoreCase("Programming Fundamentals").orElse(null);
        Task git = taskRepository.findByNameIgnoreCase("Git").orElse(null);
        Task htmlCss = taskRepository.findByNameIgnoreCase("HTML/CSS").orElse(null);
        Task react = taskRepository.findByNameIgnoreCase("React").orElse(null);

        // Level 0: Root
        RoadmapNode fundamentalsNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(fundamentals)
                .taskName("Programming Fundamentals")
                .learningAdvice("프로그래밍 기초 개념 학습")
                .difficulty(1)
                .importance(5)
                .stepOrder(1)
                .level(0)
                .build();

        // Level 1: Git
        RoadmapNode gitNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(git)
                .taskName("Git")
                .learningAdvice("버전 관리 시스템")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(1)
                .build();

        // Level 2: HTML/CSS
        RoadmapNode htmlCssNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(htmlCss)
                .taskName("HTML/CSS")
                .learningAdvice("웹 페이지 구조와 스타일링")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(2)
                .build();

        // Level 2: React
        RoadmapNode reactNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(react)
                .taskName("React")
                .learningAdvice("컴포넌트 기반 UI 라이브러리")
                .difficulty(3)
                .importance(5)
                .stepOrder(2)
                .level(2)
                .build();

        // 트리 구조 연결
        fundamentalsNode.addChild(gitNode);
        gitNode.addChild(htmlCssNode);
        gitNode.addChild(reactNode);

        roadmap.getNodes().addAll(List.of(fundamentalsNode, gitNode, htmlCssNode, reactNode));
        roadmap = jobRoadmapRepository.save(roadmap);
        jobRoadmapRepository.flush();

        // 통계 생성
        createNodeStat(fundamentalsNode, 1, 1.0, 1.0, 3, 3);
        createNodeStat(gitNode, 1, 0.95, 2.0, 3, 3);
        createNodeStat(htmlCssNode, 1, 0.85, 3.0, 3, 3);
        createNodeStat(reactNode, 2, 0.80, 3.5, 2, 3);

        log.info("프론트엔드 개발자 로드맵 생성 완료 (노드 4개, 통계 4개)");
    }

    /**
     * 모바일 앱 개발자 로드맵 생성
     * Programming Fundamentals -> Git -> Kotlin, Java
     */
    private void createSimpleMobileRoadmap(Job mobileJob) {
        JobRoadmap roadmap = JobRoadmap.builder()
                .job(mobileJob)
                .build();
        roadmap = jobRoadmapRepository.save(roadmap);

        Task fundamentals = taskRepository.findByNameIgnoreCase("Programming Fundamentals").orElse(null);
        Task git = taskRepository.findByNameIgnoreCase("Git").orElse(null);
        Task kotlin = taskRepository.findByNameIgnoreCase("Kotlin").orElse(null);
        Task java = taskRepository.findByNameIgnoreCase("Java").orElse(null);

        // Level 0: Root
        RoadmapNode fundamentalsNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(fundamentals)
                .taskName("Programming Fundamentals")
                .learningAdvice("프로그래밍 기초 개념 학습")
                .difficulty(1)
                .importance(5)
                .stepOrder(1)
                .level(0)
                .build();

        // Level 1: Git
        RoadmapNode gitNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(git)
                .taskName("Git")
                .learningAdvice("버전 관리 시스템")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(1)
                .build();

        // Level 2: Kotlin
        RoadmapNode kotlinNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(kotlin)
                .taskName("Kotlin")
                .learningAdvice("안드로이드 앱 개발 언어")
                .difficulty(3)
                .importance(5)
                .stepOrder(1)
                .level(2)
                .build();

        // Level 2: Java
        RoadmapNode javaNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(java)
                .taskName("Java")
                .learningAdvice("안드로이드 앱 개발 기본 언어")
                .difficulty(3)
                .importance(4)
                .stepOrder(2)
                .level(2)
                .build();

        // 트리 구조 연결
        fundamentalsNode.addChild(gitNode);
        gitNode.addChild(kotlinNode);
        gitNode.addChild(javaNode);

        roadmap.getNodes().addAll(List.of(fundamentalsNode, gitNode, kotlinNode, javaNode));
        roadmap = jobRoadmapRepository.save(roadmap);
        jobRoadmapRepository.flush();

        // 통계 생성
        createNodeStat(fundamentalsNode, 1, 1.0, 1.0, 3, 3);
        createNodeStat(gitNode, 1, 0.95, 2.0, 3, 3);
        createNodeStat(kotlinNode, 1, 0.90, 3.0, 3, 3);
        createNodeStat(javaNode, 2, 0.75, 3.5, 2, 3);

        log.info("모바일 앱 개발자 로드맵 생성 완료 (노드 4개, 통계 4개)");
    }

    /**
     * 데이터 엔지니어 로드맵 생성
     * Programming Fundamentals -> Git -> Python, PostgreSQL
     */
    private void createSimpleDataEngineerRoadmap(Job dataJob) {
        JobRoadmap roadmap = JobRoadmap.builder()
                .job(dataJob)
                .build();
        roadmap = jobRoadmapRepository.save(roadmap);

        Task fundamentals = taskRepository.findByNameIgnoreCase("Programming Fundamentals").orElse(null);
        Task git = taskRepository.findByNameIgnoreCase("Git").orElse(null);
        Task python = taskRepository.findByNameIgnoreCase("Python").orElse(null);
        Task postgresql = taskRepository.findByNameIgnoreCase("PostgreSQL").orElse(null);

        // Level 0: Root
        RoadmapNode fundamentalsNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(fundamentals)
                .taskName("Programming Fundamentals")
                .learningAdvice("프로그래밍 기초 개념 학습")
                .difficulty(1)
                .importance(5)
                .stepOrder(1)
                .level(0)
                .build();

        // Level 1: Git
        RoadmapNode gitNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(git)
                .taskName("Git")
                .learningAdvice("버전 관리 시스템")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(1)
                .build();

        // Level 2: Python
        RoadmapNode pythonNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(python)
                .taskName("Python")
                .learningAdvice("데이터 처리 및 분석 언어")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(2)
                .build();

        // Level 2: PostgreSQL
        RoadmapNode postgresqlNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(postgresql)
                .taskName("PostgreSQL")
                .learningAdvice("데이터 저장 및 관리")
                .difficulty(3)
                .importance(5)
                .stepOrder(2)
                .level(2)
                .build();

        // 트리 구조 연결
        fundamentalsNode.addChild(gitNode);
        gitNode.addChild(pythonNode);
        gitNode.addChild(postgresqlNode);

        roadmap.getNodes().addAll(List.of(fundamentalsNode, gitNode, pythonNode, postgresqlNode));
        roadmap = jobRoadmapRepository.save(roadmap);
        jobRoadmapRepository.flush();

        // 통계 생성
        createNodeStat(fundamentalsNode, 1, 1.0, 1.0, 3, 3);
        createNodeStat(gitNode, 1, 0.95, 2.0, 3, 3);
        createNodeStat(pythonNode, 1, 0.90, 3.0, 3, 3);
        createNodeStat(postgresqlNode, 2, 0.80, 3.5, 2, 3);

        log.info("데이터 엔지니어 로드맵 생성 완료 (노드 4개, 통계 4개)");
    }

    /**
     * AI/ML 엔지니어 로드맵 생성
     * Programming Fundamentals -> Git -> Python, FastAPI
     */
    private void createSimpleAiMlRoadmap(Job aiJob) {
        JobRoadmap roadmap = JobRoadmap.builder()
                .job(aiJob)
                .build();
        roadmap = jobRoadmapRepository.save(roadmap);

        Task fundamentals = taskRepository.findByNameIgnoreCase("Programming Fundamentals").orElse(null);
        Task git = taskRepository.findByNameIgnoreCase("Git").orElse(null);
        Task python = taskRepository.findByNameIgnoreCase("Python").orElse(null);
        Task fastapi = taskRepository.findByNameIgnoreCase("FastAPI").orElse(null);

        // Level 0: Root
        RoadmapNode fundamentalsNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(fundamentals)
                .taskName("Programming Fundamentals")
                .learningAdvice("프로그래밍 기초 개념 학습")
                .difficulty(1)
                .importance(5)
                .stepOrder(1)
                .level(0)
                .build();

        // Level 1: Git
        RoadmapNode gitNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(git)
                .taskName("Git")
                .learningAdvice("버전 관리 시스템")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(1)
                .build();

        // Level 2: Python
        RoadmapNode pythonNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(python)
                .taskName("Python")
                .learningAdvice("AI/ML 개발 주요 언어")
                .difficulty(2)
                .importance(5)
                .stepOrder(1)
                .level(2)
                .build();

        // Level 2: FastAPI
        RoadmapNode fastapiNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.JOB)
                .task(fastapi)
                .taskName("FastAPI")
                .learningAdvice("모델 서빙용 웹 프레임워크")
                .difficulty(3)
                .importance(4)
                .stepOrder(2)
                .level(2)
                .build();

        // 트리 구조 연결
        fundamentalsNode.addChild(gitNode);
        gitNode.addChild(pythonNode);
        gitNode.addChild(fastapiNode);

        roadmap.getNodes().addAll(List.of(fundamentalsNode, gitNode, pythonNode, fastapiNode));
        roadmap = jobRoadmapRepository.save(roadmap);
        jobRoadmapRepository.flush();

        // 통계 생성
        createNodeStat(fundamentalsNode, 1, 1.0, 1.0, 3, 3);
        createNodeStat(gitNode, 1, 0.95, 2.0, 3, 3);
        createNodeStat(pythonNode, 1, 0.92, 3.0, 3, 3);
        createNodeStat(fastapiNode, 2, 0.70, 3.5, 2, 3);

        log.info("AI/ML 엔지니어 로드맵 생성 완료 (노드 4개, 통계 4개)");
    }
}
