package com.back.domain.roadmap.roadmap.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.roadmap.roadmap.dto.request.MentorRoadmapSaveRequest;
import com.back.domain.roadmap.roadmap.dto.response.MentorRoadmapSaveResponse;
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
                    - 멘토는 하나의 로드맵만 생성 가능
                    - TaskId는 nullable (DB에 있는 Task 중 선택하게 하고, 원하는 Task 없는 경우 null 가능)
                    - TaskName은 필수 (표시용 이름. DB에 있는 Task 선택시 해당 taskName으로 저장, 없는 경우 입력한 이름으로 저장)
                    - stepOrder는 1부터 시작하는 연속된 숫자로, 로드맵 상 노드의 순서를 나타냄
                    - 노드들은 stepOrder 순으로 자동 정렬(멘토 로드맵은 선형으로만 구성)

                    사용 시나리오:
                    1. TaskController로 Task 검색
                    2. Task 선택 시 TaskId와 TaskName 획득
                    3. Task 없는 경우 TaskId null, TaskName 직접 입력
                    4. 노드 설명과 입력
                    """
    )
    @PostMapping
    public RsData<MentorRoadmapSaveResponse> create(@Valid @RequestBody MentorRoadmapSaveRequest request) {
        Member member = validateMentorAuth();

        MentorRoadmapSaveResponse response = mentorRoadmapService.create(member.getId(), request);

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

                    반환 정보:
                    - 로드맵 기본 정보 (로드맵 ID, 멘토 ID, 제목, 설명, 생성일, 수정일 등)
                    - 모든 노드 정보 (stepOrder 순으로 정렬)
                    """
    )
    @GetMapping("/{id}")
    public RsData<MentorRoadmapResponse> getByMentorId(@PathVariable Long id) {
        MentorRoadmapResponse response = mentorRoadmapService.getById(id);

        return new RsData<>(
                "200",
                "멘토 로드맵 조회 성공",
                response
        );
    }

    @Operation(summary = "멘토 로드맵 수정", description = "로드맵 ID로 로드맵을 찾아 수정합니다. 본인이 생성한 로드맵만 수정할 수 있습니다.")
    @PutMapping("/{id}")
    public RsData<MentorRoadmapSaveResponse> update(@PathVariable Long id, @Valid @RequestBody MentorRoadmapSaveRequest request) {
        Member member = validateMentorAuth();

        MentorRoadmapSaveResponse response = mentorRoadmapService.update(id, member.getId(), request);

        return new RsData<>(
                "200",
                "멘토 로드맵이 성공적으로 수정되었습니다.",
                response
        );
    }

    @Operation(summary = "멘토 로드맵 삭제", description = "로드맵 ID로 로드맵을 삭제합니다. 본인이 생성한 로드맵만 삭제할 수 있습니다.")
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

    // 멘토 권한 검증
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