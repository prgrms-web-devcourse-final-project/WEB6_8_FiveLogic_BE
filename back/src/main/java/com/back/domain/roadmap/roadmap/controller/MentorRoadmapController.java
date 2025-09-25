package com.back.domain.roadmap.roadmap.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapCreateRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapCreateResponse;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapResponse;
import com.back.domain.roadmap.roadmap.service.MentorRoadmapService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "MentorRoadmap", description = "멘토 로드맵 관리 API")
@RestController
@RequestMapping("/mentor-roadmaps")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
public class MentorRoadmapController {
    private final MentorRoadmapService mentorRoadmapService;
    private final Rq rq;

    @Operation(
            summary = "멘토 로드맵 생성",
            description = """
                    멘토가 자신의 커리어 로드맵을 생성합니다.

                    **주요 특징:**
                    - 멘토는 하나의 로드맵만 생성 가능
                    - TaskId는 선택사항 (기존 Task와 연결하거나 null 가능)
                    - TaskName은 필수 (표시용 이름)
                    - 노드들은 stepOrder 순으로 자동 정렬

                    **사용 시나리오:**
                    1. TaskController로 Task 검색/생성
                    2. 선택된 TaskId + TaskName으로 로드맵 생성
                    """
    )
    @PostMapping
    public RsData<MentorRoadmapCreateResponse> create(@Valid @RequestBody MentorRoadmapCreateRequest request) {
        Member member = validateMentorAuth();

        MentorRoadmapCreateResponse response = mentorRoadmapService.create(member.getId(), request);

        return new RsData<>(
                "201",
                "멘토 로드맵이 성공적으로 생성되었습니다.",
                response
        );
    }

    @Operation(
            summary = "멘토 로드맵 상세 조회",
            description = """
                    로드맵 ID로 멘토 로드맵 상세 정보를 조회합니다.

                    **반환 정보:**
                    - 로드맵 기본 정보 (제목, 설명, 생성일 등)
                    - 모든 노드 정보 (stepOrder 순으로 정렬)
                    - Task 연결 정보 (표준 Task와 연결된 경우)
                    """
    )
    @GetMapping("/{id}")
    public RsData<MentorRoadmapResponse> getById(
            @Parameter(description = "로드맵 ID", required = true)
            @PathVariable Long id) {

        MentorRoadmapResponse response = mentorRoadmapService.getById(id);

        return new RsData<>(
                "200",
                "멘토 로드맵 조회 성공",
                response
        );
    }

    @Operation(
            summary = "본인의 멘토 로드맵 조회",
            description = """
                    현재 로그인한 멘토의 로드맵을 조회합니다.

                    **응답 케이스:**
                    - 로드맵이 있는 경우: 200 상태코드로 로드맵 정보 반환
                    - 로드맵이 없는 경우: 200 상태코드로 null 반환
                    """
    )
    @GetMapping("/my")
    public RsData<MentorRoadmapResponse> getMy() {
        Member member = validateMentorAuth();

        Optional<MentorRoadmapResponse> responseOpt = mentorRoadmapService.getByMentorId(member.getId());

        if (responseOpt.isEmpty()) {
            return new RsData<>(
                    "200",
                    "생성된 로드맵이 없습니다.",
                    null
            );
        }

        return new RsData<>(
                "200",
                "내 로드맵 조회 성공",
                responseOpt.get()
        );
    }

    @Operation(
            summary = "멘토 로드맵 삭제",
            description = """
                    멘토 로드맵을 삭제합니다.

                    **권한:**
                    - 본인이 생성한 로드맵만 삭제 가능
                    - 멘토 권한 필요

                    **삭제 범위:**
                    - 로드맵과 관련된 모든 노드가 함께 삭제됨
                    """
    )
    @DeleteMapping("/{id}")
    public RsData<Void> delete(
            @Parameter(description = "로드맵 ID", required = true)
            @PathVariable Long id) {

        Member member = validateMentorAuth();

        mentorRoadmapService.delete(id, member.getId());

        return new RsData<>(
                "200",
                "멘토 로드맵이 성공적으로 삭제되었습니다.",
                null
        );
    }

    /**
     * 멘토 권한 검증
     */
    private Member validateMentorAuth() {
        Member member = rq.getActor();
        if (member == null) {
            throw new ServiceException("401", "로그인이 필요합니다.");
        }
        if (member.getRole() != Member.Role.MENTOR) {
            throw new ServiceException("403", "멘토만 접근 가능합니다.");
        }
        return member;
    }
}