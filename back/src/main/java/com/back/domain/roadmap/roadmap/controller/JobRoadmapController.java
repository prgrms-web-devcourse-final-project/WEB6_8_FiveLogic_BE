package com.back.domain.roadmap.roadmap.controller;

import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapListResponse;
import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapPagingResponse;
import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapResponse;
import com.back.domain.roadmap.roadmap.service.JobRoadmapService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-roadmaps")
@RequiredArgsConstructor
@Tag(name = "JobRoadmap Controller", description = "직업별 통합 로드맵 관련 API")
public class JobRoadmapController {
    private final JobRoadmapService jobRoadmapService;

    @GetMapping
    @Operation(
            summary = "직업 로드맵 다건 조회",
            description = "직업 로드맵 목록을 페이징과 키워드 검색으로 조회합니다."
    )
    public RsData<JobRoadmapPagingResponse> getJobRoadmaps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Page<JobRoadmapListResponse> jobRoadmapPage = jobRoadmapService.getJobRoadmaps(keyword, page, size);
        JobRoadmapPagingResponse response = JobRoadmapPagingResponse.from(jobRoadmapPage);

        return new RsData<>("200", "직업 로드맵 목록 조회 성공", response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "직업 로드맵 상세 조회",
            description = "특정 직업 로드맵의 상세 정보(직업 정보 + 모든 노드)를 조회합니다."
    )
    public RsData<JobRoadmapResponse> getJobRoadmapById(@PathVariable Long id) {
        JobRoadmapResponse roadmap = jobRoadmapService.getJobRoadmapById(id);
        return new RsData<>("200", "직업 로드맵 상세 조회 성공", roadmap);
    }
}