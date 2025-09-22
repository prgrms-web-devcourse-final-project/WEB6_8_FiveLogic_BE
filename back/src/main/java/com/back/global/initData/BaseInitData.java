package com.back.global.initData;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.service.JobService;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;

    private final JobService jobService;
    private final TaskService taskService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.initJobData();
            self.initTaskData();
        };
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

        // 백엔드 핵심 (5개)
        Task java = taskService.create("Java");
        Task springBoot = taskService.create("Spring Boot");
        Task mysql = taskService.create("MySQL");
        Task python = taskService.create("Python");
        Task nodejs = taskService.create("Node.js");

        // 프론트엔드 핵심 (5개)
        Task htmlCss = taskService.create("HTML/CSS");
        Task javascript = taskService.create("JavaScript");
        Task react = taskService.create("React");
        Task typescript = taskService.create("TypeScript");
        Task vue = taskService.create("Vue.js");

        // 공통 도구 (3개)
        Task git = taskService.create("Git");
        Task docker = taskService.create("Docker");
        Task aws = taskService.create("AWS");

        // 핵심 별칭만 (테스트용)
        taskService.createAlias(java, "자바");
        taskService.createAlias(javascript, "자바스크립트");
        taskService.createAlias(javascript, "JS");
        taskService.createAlias(react, "리액트");
        taskService.createAlias(springBoot, "스프링부트");
        taskService.createAlias(mysql, "마이SQL");
        taskService.createAlias(vue, "뷰");
        taskService.createAlias(git, "깃");
        taskService.createAlias(docker, "도커");

        // 일단 개발 및 테스트에 필요한 최소 데이터만 입력, 이후에 추가 예정
    }
}
