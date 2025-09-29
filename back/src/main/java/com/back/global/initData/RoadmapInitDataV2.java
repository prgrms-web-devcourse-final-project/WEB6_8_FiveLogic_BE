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
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.roadmap.service.MentorRoadmapService;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.domain.roadmap.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            runInitData();
        };
    }

    @Transactional
    public void runInitData() {
        initJobData();
        initTaskData();
        //initSampleMentorRoadmaps();
    }

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

        // 일단 개발 및 테스트에 필요한 최소 데이터만 입력, 이후에 추가 예정
    }

    public void initTaskData() {
        if (taskService.count() > 0) return;

        // 프로그래밍 언어 (6개)
        Task java = taskService.create("Java");
        Task python = taskService.create("Python");
        Task javascript = taskService.create("JavaScript");
        Task go = taskService.create("Go");
        Task kotlin = taskService.create("Kotlin");
        Task csharp = taskService.create("C");

        // 백엔드 프레임워크 (8개)
        Task springBoot = taskService.create("Spring Boot");
        Task springMvc = taskService.create("Spring MVC");
        Task springSecurity = taskService.create("Spring Security");
        Task jpa = taskService.create("JPA");
        Task django = taskService.create("Django");
        Task fastapi = taskService.create("FastAPI");
        Task expressjs = taskService.create("Express.js");
        Task nodejs = taskService.create("Node.js");

        // 데이터베이스 (5개)
        Task mysql = taskService.create("MySQL");
        Task postgresql = taskService.create("PostgreSQL");
        Task mongodb = taskService.create("MongoDB");
        Task redis = taskService.create("Redis");
        Task oracle = taskService.create("Oracle");

        // 빌드 도구 및 테스트 (5개)
        Task gradle = taskService.create("Gradle");
        Task maven = taskService.create("Maven");
        Task junit = taskService.create("JUnit");
        Task mockito = taskService.create("Mockito");
        Task postman = taskService.create("Postman");

        // DevOps 및 인프라 (7개)
        Task git = taskService.create("Git");
        Task docker = taskService.create("Docker");
        Task kubernetes = taskService.create("Kubernetes");
        Task aws = taskService.create("AWS");
        Task jenkins = taskService.create("Jenkins");
        Task nginx = taskService.create("Nginx");
        Task linux = taskService.create("Linux");

        // 프론트엔드 (5개)
        Task htmlCss = taskService.create("HTML/CSS");
        Task react = taskService.create("React");
        Task vue = taskService.create("Vue.js");
        Task typescript = taskService.create("TypeScript");
        Task nextjs = taskService.create("Next.js");

        // 백엔드 관련 별칭 추가
        taskService.createAlias(java, "자바");
        taskService.createAlias(springBoot, "스프링부트");
        taskService.createAlias(springBoot, "스프링 부트");
        taskService.createAlias(springSecurity, "스프링 시큐리티");
        taskService.createAlias(mysql, "마이SQL");
        taskService.createAlias(mysql, "MySQL");
        taskService.createAlias(postgresql, "포스트그레SQL");
        taskService.createAlias(postgresql, "PostgreSQL");
        taskService.createAlias(mongodb, "몽고DB");
        taskService.createAlias(redis, "레디스");
        taskService.createAlias(docker, "도커");
        taskService.createAlias(kubernetes, "쿠버네티스");
        taskService.createAlias(kubernetes, "k8s");
        taskService.createAlias(git, "깃");
        taskService.createAlias(aws, "아마존웹서비스");
        taskService.createAlias(javascript, "자바스크립트");
        taskService.createAlias(javascript, "JS");
        taskService.createAlias(python, "파이썬");
        taskService.createAlias(django, "장고");
        taskService.createAlias(fastapi, "FastAPI");
    }

    public void initSampleMentorRoadmaps() {
        // 이미 멘토 로드맵이 있으면 실행하지 않음
        if (mentorRoadmapRepository.count() > 0) return;

        // 백엔드 개발자 직업 조회
        Job backendJob = jobRepository.findByName("백엔드 개발자")
                .orElseThrow(() -> new RuntimeException("백엔드 개발자 직업을 찾을 수 없습니다."));

        // 샘플 멘토 생성 및 로드맵 구축
        createJavaTraditionalRoadmap(backendJob);
        createJavaModernRoadmap(backendJob);
        createJavaEnterpriseRoadmap(backendJob);
        createPythonDjangoRoadmap(backendJob);
        createNodeJSRoadmap(backendJob);
        createFullStackRoadmap(backendJob);
        createDevOpsRoadmap(backendJob);
    }

    private void createJavaTraditionalRoadmap(Job backendJob) {
        // 샘플1
        Member member = memberService.joinMentor("mentor1@test.com", "멘토1", "test1", "1234", "백엔드 개발자", 3);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Java", 0, 1, "객체지향 프로그래밍의 기초를 확실히 잡아야 합니다. 특히 상속, 다형성, 캡슐화 개념을 실습을 통해 체득하세요."),
                createNodeRequest("Git", 0, 2, "버전 관리는 필수입니다. add, commit, push, pull 명령어와 브랜치 전략을 익히세요."),
                createNodeRequest("Spring Boot", 0, 3, "자바 백엔드의 표준 프레임워크입니다. DI와 IoC 컨테이너 개념을 이해하고 실습하세요."),
                createNodeRequest("MySQL", 0, 4, "관계형 데이터베이스의 기본입니다. SQL 쿼리 최적화와 인덱스 개념을 반드시 익히세요."),
                createNodeRequest("JPA", 0, 5, "ORM을 통해 데이터베이스와 객체를 매핑합니다. N+1 문제와 지연로딩을 꼭 이해하세요."),
                createNodeRequest("Spring Security", 0, 6, "인증과 인가를 담당합니다. JWT 토큰 방식을 먼저 익힌 후 OAuth2를 학습하세요."),
                createNodeRequest("JUnit", 0, 7, "테스트 코드 작성은 필수 스킬입니다. 단위 테스트부터 시작해서 통합 테스트까지 익히세요.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "전통적인 자바 백엔드 로드맵",
                "은행, 대기업에서 주로 사용하는 안정적인 자바 백엔드 스택입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createJavaModernRoadmap(Job backendJob) {
        // 샘플2
        Member member = memberService.joinMentor("mentor2@test.com", "멘토2", "test2", "1234", "백엔드 개발자", 5);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Java", 0, 1, "Java 11 이상의 최신 문법을 익히세요. Stream API와 Optional 활용이 중요합니다."),
                createNodeRequest("Git", 0, 2, "Git Flow나 GitHub Flow 전략을 익히고, 코드 리뷰 문화에 익숙해지세요."),
                createNodeRequest("Spring Boot", 0, 3, "Spring Boot 2.7+ 버전의 Auto Configuration과 Actuator를 활용하세요."),
                createNodeRequest("PostgreSQL", 0, 4, "MySQL보다 풍부한 기능을 제공합니다. JSON 타입과 고급 쿼리를 활용해보세요."),
                createNodeRequest("Gradle", 0, 5, "Maven보다 유연한 빌드 도구입니다. 멀티 모듈 프로젝트 구성을 익히세요."),
                createNodeRequest("Docker", 0, 6, "컨테이너화는 현대 개발의 필수입니다. Docker Compose로 로컬 개발환경을 구성하세요."),
                createNodeRequest("Redis", 0, 7, "캐싱과 세션 관리에 활용합니다. 데이터 타입별 사용법과 성능 최적화를 익히세요."),
                createNodeRequest("AWS", 0, 8, "클라우드 환경에서의 배포와 운영을 익힙니다. EC2, RDS, S3부터 시작하세요.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "모던 자바 백엔드 로드맵",
                "최신 기술 스택을 활용한 확장 가능한 자바 백엔드 개발 로드맵입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createJavaEnterpriseRoadmap(Job backendJob) {
        // 샘플3
        Member member = memberService.joinMentor("mentor3@test.com", "멘토3", "test3", "1234", "백엔드 개발자", 6);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Java", 0, 1, "Java 17 LTS 버전을 기준으로 학습하세요. 성능과 메모리 관리가 중요합니다."),
                createNodeRequest("Spring Boot", 0, 2, "Spring Boot의 설정 관리와 프로파일 활용을 마스터하세요."),
                createNodeRequest("Oracle", 0, 3, "엔터프라이즈 환경에서 많이 사용됩니다. PL/SQL과 성능 튜닝을 익히세요."),
                createNodeRequest("Maven", 0, 4, "엔터프라이즈 표준 빌드 도구입니다. 의존성 관리와 멀티 모듈을 익히세요."),
                createNodeRequest("Spring Security", 0, 5, "엔터프라이즈급 보안을 구현합니다. LDAP 연동과 권한 관리를 익히세요."),
                createNodeRequest("JPA", 0, 6, "복잡한 도메인 모델링과 성능 최적화가 핵심입니다. 쿼리 튜닝을 익히세요."),
                createNodeRequest("JUnit", 0, 7, "코드 커버리지 80% 이상을 목표로 체계적인 테스트를 작성하세요."),
                createNodeRequest("Linux", 0, 8, "운영 서버 환경 이해가 필요합니다. 기본 명령어와 스크립트 작성을 익히세요.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "엔터프라이즈 자바 로드맵",
                "대규모 기업 환경에서 요구되는 자바 백엔드 개발 로드맵입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createPythonDjangoRoadmap(Job backendJob) {
        // 샘플4
        Member member = memberService.joinMentor("mentor4@test.com", "멘토4", "test4", "1234", "백엔드 개발자", 4);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Python", 0, 1, "파이썬 기본 문법과 객체지향을 익히세요. 특히 데코레이터와 컨텍스트 매니저를 이해하세요."),
                createNodeRequest("Git", 0, 2, "브랜치 전략과 충돌 해결 방법을 익히세요. Python 프로젝트의 .gitignore 설정도 중요합니다."),
                createNodeRequest("Django", 0, 3, "MVT 패턴을 이해하고 Django ORM을 활용하세요. Admin 인터페이스가 강력한 장점입니다."),
                createNodeRequest("PostgreSQL", 0, 4, "Django와 궁합이 좋습니다. 마이그레이션 관리와 쿼리 최적화를 익히세요."),
                createNodeRequest("Django", 0, 5, "Django REST Framework로 API를 구축하세요. 시리얼라이저와 뷰셋 활용이 핵심입니다."),
                createNodeRequest("Redis", 0, 6, "Django 캐싱과 Celery 작업 큐에 활용합니다. 비동기 작업 처리를 익히세요."),
                createNodeRequest("Docker", 0, 7, "Python 가상환경의 한계를 보완합니다. requirements.txt와 Dockerfile 작성을 익히세요.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "파이썬 Django 백엔드 로드맵",
                "빠른 개발과 유지보수가 가능한 파이썬 Django 백엔드 로드맵입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createNodeJSRoadmap(Job backendJob) {
        // 샘플5
        Member member = memberService.joinMentor("mentor5@test.com", "멘토5", "test5", "1234", "백엔드 개발자", 2);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("JavaScript", 0, 1, "ES6+ 문법과 비동기 처리(Promise, async/await)를 확실히 익히세요."),
                createNodeRequest("Node.js", 0, 2, "이벤트 루프와 Non-blocking I/O를 이해하는 것이 핵심입니다."),
                createNodeRequest("Express.js", 0, 3, "Node.js의 대표 웹 프레임워크입니다. 미들웨어 구조를 이해하고 활용하세요."),
                createNodeRequest("MongoDB", 0, 4, "NoSQL 데이터베이스로 JSON과 유사한 구조라 JavaScript와 궁합이 좋습니다."),
                createNodeRequest("MongoDB", 0, 5, "Mongoose ODM을 활용해 스키마 관리와 데이터 검증을 구현하세요."),
                createNodeRequest("Docker", 0, 6, "Node.js 애플리케이션의 배포와 확장성을 위해 컨테이너화하세요."),
                createNodeRequest("AWS", 0, 7, "Lambda를 활용한 서버리스 아키텍처를 경험해보세요.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "Node.js 백엔드 로드맵",
                "빠른 프로토타이핑과 실시간 기능에 강한 Node.js 백엔드 로드맵입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createFullStackRoadmap(Job backendJob) {
        // 샘플6
        Member member = memberService.joinMentor("mentor6@test.com", "멘토6", "test6", "1234", "백엔드 개발자", 6);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Java", 0, 1, "백엔드의 안정성을 위해 Java를 선택했습니다. 타입 안정성이 큰 장점입니다."),
                createNodeRequest("JavaScript", 0, 2, "프론트엔드와의 원활한 소통을 위해 JavaScript도 필수입니다."),
                createNodeRequest("Spring Boot", 0, 3, "API 서버 구축의 핵심입니다. RESTful API 설계 원칙을 지키세요."),
                createNodeRequest("MySQL", 0, 4, "관계형 데이터베이스 설계와 정규화를 이해하세요."),
                createNodeRequest("React", 0, 5, "프론트엔드 이해를 위해 React를 학습합니다. 컴포넌트 기반 개발을 익히세요."),
                createNodeRequest("JPA", 0, 6, "백엔드와 데이터베이스 간의 효율적인 데이터 처리를 위해 필수입니다."),
                createNodeRequest("Docker", 0, 7, "로컬 개발환경과 배포환경의 일관성을 위해 컨테이너화합니다."),
                createNodeRequest("Kubernetes", 0, 8, "마이크로서비스 아키텍처에서 컨테이너 오케스트레이션을 담당합니다.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "풀스택 백엔드 로드맵",
                "백엔드 중심이지만 프론트엔드도 이해하는 풀스택 개발자 로드맵입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    private void createDevOpsRoadmap(Job backendJob) {
        // 샘플7
        Member member = memberService.joinMentor("mentor7@test.com", "멘토7", "test7", "1234", "백엔드 개발자", 7);
        Mentor mentor = updateMentorJob(member, backendJob);

        // 로드맵 노드 리스트 생성
        List<RoadmapNodeRequest> nodes = List.of(
                createNodeRequest("Java", 0, 1, "안정적인 서비스 운영을 위해 Java의 메모리 관리와 GC 튜닝을 이해하세요."),
                createNodeRequest("Linux", 0, 2, "서버 운영의 기본입니다. 시스템 관리와 네트워크 설정을 익히세요."),
                createNodeRequest("Spring Boot", 0, 3, "운영 환경에서의 모니터링과 헬스체크 기능을 활용하세요."),
                createNodeRequest("MySQL", 0, 4, "데이터베이스 성능 모니터링과 백업 전략을 수립하세요."),
                createNodeRequest("Git", 0, 5, "GitOps 방식의 배포 파이프라인 구축에 활용합니다."),
                createNodeRequest("Docker", 0, 6, "애플리케이션 컨테이너화로 배포 일관성을 확보하세요."),
                createNodeRequest("Kubernetes", 0, 7, "프로덕션 환경에서의 무중단 배포와 오토스케일링을 구현하세요."),
                createNodeRequest("AWS", 0, 8, "클라우드 인프라 설계와 비용 최적화를 고려하세요."),
                createNodeRequest("Jenkins", 0, 9, "CI/CD 파이프라인을 구축해 자동화된 배포 시스템을 만드세요."),
                createNodeRequest("Nginx", 0, 10, "로드 밸런싱과 리버스 프록시 설정으로 시스템 안정성을 높이세요.")
        );

        MentorRoadmapSaveRequest request = new MentorRoadmapSaveRequest(
                "DevOps 포함 백엔드 로드맵",
                "개발부터 배포, 운영까지 전체적인 시스템 라이프사이클을 다루는 로드맵입니다.",
                nodes
        );

        mentorRoadmapService.create(mentor.getId(), request);
    }

    // 헬퍼 메서드들

    private Mentor updateMentorJob(Member member, Job job) {
        Mentor mentor = mentorRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new RuntimeException("멘토를 찾을 수 없습니다: " + member.getId()));

        // 새로운 Mentor 객체를 jobId와 함께 생성하여 저장
        Mentor updatedMentor = Mentor.builder()
                .member(mentor.getMember())
                .jobId(job.getId())
                .careerYears(mentor.getCareerYears())
                .rate(mentor.getRate())
                .build();

        // 기존 mentor 삭제 후 새로운 mentor 저장
        mentorRepository.delete(mentor);
        return mentorRepository.save(updatedMentor);
    }

    private RoadmapNodeRequest createNodeRequest(String taskName, int level, int stepOrder, String description) {
        Task task = taskRepository.findByNameIgnoreCase(taskName)
                .orElse(null); // Task가 없으면 null로 처리 (taskName으로 표시)

        return new RoadmapNodeRequest(
                task != null ? task.getId() : null,
                taskName,
                description,
                stepOrder
        );
    }
}
