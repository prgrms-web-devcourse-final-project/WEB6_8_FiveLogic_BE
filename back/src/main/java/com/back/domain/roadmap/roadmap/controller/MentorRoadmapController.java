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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mentor-roadmaps")
@RequiredArgsConstructor
@Tag(name = "MentorRoadmap", description = "멘토 로드맵 관리 API")
public class MentorRoadmapController {
    private final MentorRoadmapService mentorRoadmapService;
    private final Rq rq;

    @Operation(
            summary = "멘토 로드맵 생성",
            description = """
                    멘토가 자신의 커리어 로드맵을 생성합니다.

                    **주요 특징:**
                    - 멘토는 하나의 로드맵만 생성 가능
                    - TaskId는 nullable (DB에 있는 Task 중 선택하게 하고, 원하는 Task 없는 경우 null 가능)
                    - TaskName은 필수 (표시용 이름. DB에 있는 Task 선택시 해당 taskName으로 저장, 없는 경우 입력한 이름으로 저장)
                    - 노드들은 stepOrder 순으로 자동 정렬(멘토 로드맵은 선형으로만 구성)

                    **사용 시나리오:**
                    1. TaskController로 Task 검색
                    2. Task 선택 시 TaskId와 TaskName 획득
                    3. Task 없는 경우 TaskId null, TaskName 직접 입력
                    4. 노드 설명과 입력
                    5. stepOrder는 1부터 시작하는 연속된 숫자로 자동으로 설정
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
                    멘토 ID로 멘토 로드맵 상세 정보를 조회합니다.

                    **반환 정보:**
                    - 로드맵 기본 정보 (제목, 설명, 생성일 등)
                    - 모든 노드 정보 (stepOrder 순으로 정렬)
                    - Task 연결 정보 (표준 Task와 연결된 경우)
                    """
    )
    @GetMapping("/{mentorId}")
    public RsData<MentorRoadmapResponse> getByMentorId(@PathVariable Long mentorId) {
        MentorRoadmapResponse response = mentorRoadmapService.getByMentorId(mentorId);

        return new RsData<>(
                "200",
                "멘토 로드맵 조회 성공",
                response
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
    public RsData<Void> delete( @PathVariable Long id) {

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