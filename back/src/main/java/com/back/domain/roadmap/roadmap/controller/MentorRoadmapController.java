package com.back.domain.roadmap.roadmap.controller;

import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapSaveRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapSaveResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapResponse;
import com.back.domain.roadmap.roadmap.service.MentorRoadmapService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentor-roadmaps")
@RequiredArgsConstructor
@Tag(name = "MentorRoadmapController", description = "멘토 로드맵 API")
public class MentorRoadmapController {
    private final MentorRoadmapService mentorRoadmapService;
    private final MemberStorage memberStorage;
    private final Rq rq;

    @Operation(
            summary = "멘토 로드맵 생성",
            description = """
                    ### 개요
                    멘토가 자신의 커리어 여정을 단계별로 기록한 로드맵을 생성합니다.

                    ### 제약 사항
                    - **멘토당 1개의 로드맵만 생성 가능** (중복 시 409 에러)
                    - **최소 1개 이상의 노드** 필요
                    - **stepOrder는 1부터 시작하는 연속된 숫자** (예: 1,2,3,...)
                    - **멘토 권한(MENTOR)** 필요

                    ### 노드(RoadmapNode) 구성

                    **필수 필드:**
                    - `taskName`: 기술/단계 이름 (최대 100자)
                    - `stepOrder`: 로드맵 상 순서 (1부터 시작, 중복 불가)

                    **선택 필드:**
                    - `taskId`: 표준 Task ID (nullable)
                      - Task 검색(`/tasks/search`)으로 선택 가능
                      - null이면 자동으로 pending alias 등록 (관리자 승인 대기)
                    - `learningAdvice`: 학습 조언/방법 (최대 2000자)
                    - `recommendedResources`: 추천 자료 (최대 2000자)
                    - `learningGoals`: 학습 목표 (최대 1000자)
                    - `difficulty`: 난이도 (1-5)
                    - `importance`: 중요도 (1-5)
                    - `hoursPerDay`: 하루 학습 시간
                    - `weeks`: 학습 주차
                    - `estimatedHours`: 자동 계산 (hoursPerDay × weeks × 7)

                    ### 사용 시나리오
                    1. `/tasks/search`로 Task 검색
                    2. 원하는 Task 선택 → taskId, taskName 획득
                    3. 원하는 Task 없으면 → taskId는 null, taskName 직접 입력
                    4. 각 단계별 학습 정보 입력 (조언, 자료, 목표 등)
                    5. 로드맵 생성 요청

                    ### 응답 코드
                    - **201**: 생성 성공
                    - **400**: 유효성 검증 실패 (stepOrder 불연속/중복 등)
                    - **403**: 멘토 권한 없음
                    - **404**: 존재하지 않는 taskId 참조
                    - **409**: 이미 로드맵 존재

                    ### 참고
                    - 멘토 로드맵은 선형 구조 (순차적 학습 경로)
                    - 로드맵 생성 후 직업 로드맵 통합 이벤트 발행 (비동기)
                    """
    )
    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentorRoadmapSaveResponse> create(@Valid @RequestBody MentorRoadmapSaveRequest request) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());

        MentorRoadmapSaveResponse response = mentorRoadmapService.create(mentor.getId(), request);

        return new RsData<>(
                "201",
                "멘토 로드맵이 성공적으로 생성되었습니다.",
                response
        );
    }

    @Operation(
            summary = "멘토 로드맵 상세 조회 (로드맵 ID)",
            description = """
                    ### 개요
                    로드맵 ID로 멘토 로드맵의 전체 정보를 조회합니다.

                    ### 권한
                    - 로그인한 모든 사용자 조회 가능

                    ### 반환 정보
                    - 로드맵 기본 정보 (id, mentorId, title, description)
                    - 모든 노드 정보 (stepOrder 순으로 정렬)
                    - 생성일, 수정일

                    ### 노드 정보 구조
                    각 노드는 다음 정보를 포함합니다:
                    - taskId, taskName: Task 정보
                    - learningAdvice, recommendedResources, learningGoals: 학습 가이드
                    - difficulty, importance: 난이도/중요도 (1-5)
                    - hoursPerDay, weeks, estimatedHours: 학습 시간 정보
                    - stepOrder: 로드맵 상 순서

                    ### 응답 코드
                    - **200**: 조회 성공
                    - **401**: 인증 필요
                    - **404**: 로드맵을 찾을 수 없음
                    """
    )
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RsData<MentorRoadmapResponse> getById(@PathVariable Long id) {
        MentorRoadmapResponse response = mentorRoadmapService.getById(id);

        return new RsData<>(
                "200",
                "멘토 로드맵 조회 성공",
                response
        );
    }

    @Operation(
            summary = "멘토 로드맵 상세 조회 (멘토 ID)",
            description = """
                    ### 개요
                    멘토 ID로 해당 멘토의 로드맵 전체 정보를 조회합니다.

                    ### 권한
                    - 로그인한 모든 사용자 조회 가능

                    ### 반환 정보
                    - 로드맵 기본 정보 (id, mentorId, title, description)
                    - 모든 노드 정보 (stepOrder 순으로 정렬)
                    - 생성일, 수정일

                    ### 응답 코드
                    - **200**: 조회 성공
                    - **401**: 인증 필요
                    - **404**: 해당 멘토의 로드맵을 찾을 수 없음

                    ### 참고
                    - 멘토가 로드맵을 생성하지 않았으면 404 에러 발생
                    - 멘토 ID는 Member ID가 아닌 Mentor 엔티티의 ID입니다
                    """
    )
    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("isAuthenticated()")
    public RsData<MentorRoadmapResponse> getByMentorId(@PathVariable Long mentorId) {
        MentorRoadmapResponse response = mentorRoadmapService.getByMentorId(mentorId);

        return new RsData<>(
                "200",
                "멘토 로드맵 조회 성공",
                response
        );
    }

    @Operation(
            summary = "멘토 로드맵 수정",
            description = """
                    ### 개요
                    로드맵 ID로 로드맵을 찾아 전체 내용을 수정합니다.

                    ### 권한
                    - **본인이 생성한 로드맵만 수정 가능** (타인 수정 시 403 에러)
                    - 멘토 권한(MENTOR) 필요

                    ### 수정 방식
                    - **전체 교체 방식**: 기존 노드를 모두 삭제하고 새 노드로 교체
                    - 로드맵 제목, 설명도 함께 수정
                    - 생성 API와 동일한 유효성 검증 적용

                    ### 요청 형식
                    - 생성 API와 동일한 Request Body 사용
                    - 모든 노드를 다시 전송해야 함 (부분 수정 불가)

                    ### 응답 코드
                    - **200**: 수정 성공
                    - **400**: 유효성 검증 실패
                    - **403**: 본인의 로드맵이 아님
                    - **404**: 로드맵을 찾을 수 없음

                    ### 참고
                    - 수정 후 직업 로드맵 통합 이벤트 발행 (비동기)
                    - taskId가 null인 노드는 자동으로 pending alias 등록
                    """
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<MentorRoadmapSaveResponse> update(@PathVariable Long id, @Valid @RequestBody MentorRoadmapSaveRequest request) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());

        MentorRoadmapSaveResponse response = mentorRoadmapService.update(id, mentor.getId(), request);

        return new RsData<>(
                "200",
                "멘토 로드맵이 성공적으로 수정되었습니다.",
                response
        );
    }

    @Operation(
            summary = "멘토 로드맵 삭제",
            description = """
                    ### 개요
                    로드맵 ID로 로드맵을 삭제합니다.

                    ### 권한
                    - **본인이 생성한 로드맵만 삭제 가능** (타인 삭제 시 403 에러)
                    - 멘토 권한(MENTOR) 필요

                    ### 삭제 범위
                    - 로드맵과 모든 노드가 함께 삭제됩니다 (Cascade)
                    - 참조하던 Task는 삭제되지 않습니다 (Task는 공유 자원)

                    ### 응답 코드
                    - **200**: 삭제 성공
                    - **403**: 본인의 로드맵이 아님
                    - **404**: 로드맵을 찾을 수 없음

                    ### 참고
                    - 삭제 후 직업 로드맵 통합 이벤트 발행 (비동기)
                    - 삭제된 로드맵은 복구할 수 없습니다
                    """
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MENTOR')")
    public RsData<Void> delete(@PathVariable Long id) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());

        mentorRoadmapService.delete(id, mentor.getId());

        return new RsData<>(
                "200",
                "멘토 로드맵이 성공적으로 삭제되었습니다.",
                null
        );
    }

}