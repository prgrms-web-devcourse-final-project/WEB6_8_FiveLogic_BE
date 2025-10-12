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
            summary = "직업 로드맵 목록 조회",
            description = """
                    ### 개요
                    모든 직업의 로드맵 목록을 페이징과 키워드 검색으로 조회합니다.

                    ### 쿼리 파라미터
                    - `page`: 페이지 번호 (0부터 시작, 기본값: 0)
                    - `size`: 페이지 크기 (기본값: 10)
                    - `keyword`: 검색 키워드 (선택, 직업명으로 검색)

                    ### 반환 정보
                    각 직업 로드맵의 요약 정보:
                    - id: 직업 로드맵 ID
                    - jobName: 직업명
                    - jobDescription: 직업 설명

                    ### 응답 형식
                    - content: 직업 로드맵 목록
                    - totalElements: 전체 개수
                    - totalPages: 전체 페이지 수
                    - number: 현재 페이지 번호
                    - size: 페이지 크기

                    ### 응답 코드
                    - **200**: 조회 성공

                    ### 참고
                    - 인증 불필요 (누구나 조회 가능)
                    - 키워드는 직업명(jobName)에 대해 부분 일치 검색
                    """
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
            description = """
                    ### 개요
                    특정 직업의 통합 로드맵을 트리 구조로 조회합니다.
                    여러 멘토의 멘토 로드맵을 통합하여 생성된 로드맵입니다.

                    ### 직업 로드맵이란?
                    - 동일 직업의 멘토들의 로드맵을 통합한 결과
                    - 비선형 구조 (트리형) - 다양한 학습 경로 표현
                    - 멘토들의 빈도, 순서 패턴, 연결 관계를 분석하여 생성
                    - 주기적으로 자동 업데이트 (멘토 로드맵 변경 시)

                    ### 반환 정보

                    **로드맵 기본 정보:**
                    - id, jobId, jobName: 직업 정보
                    - totalNodeCount: 전체 노드 개수
                    - createdDate, modifiedDate: 생성/수정일

                    **노드 정보 (트리 구조):**

                    각 노드는 다음 정보를 포함합니다:

                    *기본 정보:*
                    - id, parentId, childIds: 트리 구조 정보
                    - taskId, taskName: Task 정보
                    - level: 트리 깊이 (0: 루트, 1: 1단계 자식...)
                    - stepOrder: 같은 부모 내 순서

                    *학습 정보 (여러 멘토의 정보 통합):*
                    - learningAdvice: 학습 조언 통합
                    - recommendedResources: 추천 자료 통합
                    - learningGoals: 학습 목표 통합
                    - difficulty: 평균 난이도 (1-5)
                    - importance: 평균 중요도 (1-5)
                    - estimatedHours: 평균 예상 학습 시간

                    *통계 정보:*
                    - weight: 노드 가중치 (빈도, 위치, 연결성 기반)
                    - mentorCount: 이 노드를 사용한 멘토 수
                    - totalMentorCount: 해당 직업의 전체 멘토 수
                    - mentorCoverageRatio: 커버리지 비율 (0.0 ~ 1.0)
                    - isEssential: 필수 노드 여부 (50% 이상)
                    - essentialLevel:
                      - "CORE": 80% 이상의 멘토가 선택 (핵심 필수)
                      - "COMMON": 50% 이상의 멘토가 선택 (일반 필수)
                      - "OPTIONAL": 50% 미만의 멘토가 선택 (선택)

                    *자식 노드:*
                    - children: 자식 노드 목록 (재귀 구조)

                    ### 응답 구조
                    - 루트 노드들만 최상위에 반환
                    - 각 노드의 children 필드에 자식 노드들이 재귀적으로 포함
                    - 전체 트리 구조를 한 번에 조회 가능

                    ### 응답 코드
                    - **200**: 조회 성공
                    - **404**: 직업 로드맵을 찾을 수 없음

                    ### 참고
                    - 로그인한 사용자만 조회 가능
                    - 통합 알고리즘: 빈도수(40%), 멘토 커버리지(30%), 위치(20%), 연결성(10%)
                    - essentialLevel로 필수/선택 경로 구분 가능
                    - 통계 정보를 활용해 학습 우선순위 판단 가능
                    """
    )
    public RsData<JobRoadmapResponse> getJobRoadmapById(@PathVariable Long id) {
        JobRoadmapResponse roadmap = jobRoadmapService.getJobRoadmapById(id);
        return new RsData<>("200", "직업 로드맵 상세 조회 성공", roadmap);
    }
}