package com.back.domain.roadmap.task.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.roadmap.task.dto.*;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.entity.TaskAlias;
import com.back.domain.roadmap.task.service.TaskService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "TaskController", description = "Task 컨트롤러")
public class TaskController {
    private final TaskService taskService;
    private final Rq rq;

    @Operation(
            summary = "키워드로 Task 검색",
            description = """
                    ### 개요
                    사용자가 입력한 키워드로 표준 Task를 검색합니다.

                    ### 검색 범위
                    - Task 이름 (표준 기술명)
                    - TaskAlias 이름 (별칭)
                    - 대소문자 구분 없이 부분 일치 검색

                    ### 쿼리 파라미터
                    - `keyword`: 검색 키워드 (필수)

                    ### 반환 정보
                    - id: Task ID
                    - name: 표준 Task 이름
                    - 연결된 TaskAlias는 반환되지 않음 (Task 정보만)

                    ### 사용 시나리오
                    1. 멘토 로드맵 생성 전 Task 검색
                    2. 원하는 기술/단계 선택
                    3. 없으면 pending alias 생성 제안

                    ### 응답 코드
                    - **200**: 검색 성공 (결과 없어도 200, 빈 배열 반환)

                    ### 참고
                    - 인증 불필요
                    - 키워드가 비어있으면 빈 배열 반환
                    - 중복 제거된 Task 목록 반환
                    """
    )
    @GetMapping("/search")
    public RsData<List<TaskDto>> searchTasks(@RequestParam String keyword) {
        // 입력값 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            return new RsData<>(
                    "200",
                    "검색 결과가 없습니다.",
                    Collections.emptyList()
            );
        }

        List<Task> tasks = taskService.searchByKeyword(keyword.trim());
        List<TaskDto> responses = tasks.stream()
                .map(TaskDto::new)
                .toList();

        return new RsData<>(
                "200",
                "Task 검색 성공",
                responses
        );
    }

    @Operation(
            summary = "사용자가 새로운 Task 제안",
            description = """
                    ### 개요
                    사용자가 원하는 Task가 검색되지 않을 때,
                    새로운 Task 이름을 제안합니다 (pending alias 생성).

                    ### Pending Alias란?
                    - 표준 Task와 아직 연결되지 않은 별칭
                    - 관리자 검토 대기 상태
                    - 관리자가 기존 Task와 연결하거나 새 Task로 승격

                    ### 요청 형식
                    - `taskName`: 제안할 Task 이름 (필수)

                    ### 검증 로직
                    1. Task 테이블에서 중복 확인 (이미 표준 Task 존재 시 400)
                    2. TaskAlias 테이블에서 중복 확인:
                       - 연결된 alias 존재 시: "이미 등록된 Task의 별칭입니다" (400)
                       - Pending alias 존재 시: "이미 제안된 Task명입니다" (400)

                    ### 응답 정보
                    - id: 생성된 TaskAlias ID
                    - name: 제안한 이름
                    - isPending: true (항상)

                    ### 응답 코드
                    - **201**: 제안 성공
                    - **400**: 이미 존재하는 이름

                    ### 참고
                    - 인증 불필요
                    - 멘토 로드맵 생성 시 taskId가 null인 경우 자동으로 pending alias 등록
                    """
    )
    @PostMapping("/aliases/pending")
    public RsData<CreatePendingAliasResponse> createPendingAlias(@Valid @RequestBody CreatePendingAliasRequest request) {
        TaskAlias pendingAlias = taskService.createPendingAlias(request.taskName().trim());

        return new RsData<>(
                "201",
                "새로운 Pending Alias 등록 성공. 관리자 검토 후 매칭 또는 새로운 Task로 등록됩니다.",
                new CreatePendingAliasResponse(pendingAlias)
        );
    }


    //=== 관리자용 API ===
    @Operation(
            summary = "pending 상태의 TaskAlias 목록 조회 (관리자)",
            description = """
                    ### 개요
                    관리자가 아직 표준 Task와 연결되지 않은
                    pending 상태의 TaskAlias 목록을 조회합니다.

                    ### 권한
                    - **관리자 권한(ADMIN) 필수**
                    - 401: 미인증
                    - 403: 권한 없음

                    ### 쿼리 파라미터
                    - `page`: 페이지 번호 (기본값: 0)
                    - `size`: 페이지 크기 (기본값: 10)
                    - `sort`: 정렬 기준 (기본값: createdDate,DESC)

                    ### 반환 정보
                    - id: TaskAlias ID
                    - name: 제안된 이름
                    - 페이징 정보 포함

                    ### 관리자 작업 플로우
                    1. pending alias 목록 조회
                    2. 각 alias 검토:
                       - 기존 Task와 유사 → linkPendingAlias (연결)
                       - 새로운 Task 필요 → createTaskFromPending (승격)
                       - 부적절 → deletePendingAlias (삭제)

                    ### 응답 코드
                    - **200**: 조회 성공
                    - **401**: 인증 필요
                    - **403**: 관리자 권한 없음
                    """
    )
    @GetMapping("/aliases/pending")
    public RsData<Page<TaskAliasDto>> getPendingTaskAliases(
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        validateAdminRole();

        Page<TaskAliasDto> pendingTaskAliases = taskService.getPendingTaskAliases(pageable);

        return  new RsData<>(
                "200",
                "pending 상태의 TaskAlias 목록 조회 성공",
                pendingTaskAliases
        );
    }

    @Operation(
            summary = "pending alias를 기존 표준 Task와 연결 (관리자)",
            description = """
                    ### 개요
                    관리자가 pending alias를 검토하여
                    기존의 표준 Task와 연결합니다.

                    ### 권한
                    - **관리자 권한(ADMIN) 필수**

                    ### 요청 형식
                    - `taskId`: 연결할 표준 Task의 ID (필수)

                    ### 처리 로직
                    1. aliasId로 TaskAlias 조회 및 pending 상태 확인
                    2. taskId로 Task 조회
                    3. TaskAlias.task 필드에 Task 연결

                    ### 응답 정보
                    - id: TaskAlias ID
                    - name: Alias 이름
                    - taskId: 연결된 Task ID
                    - taskName: 연결된 Task 이름

                    ### 응답 코드
                    - **200**: 연결 성공
                    - **400**: 이미 연결된 alias
                    - **401**: 인증 필요
                    - **403**: 관리자 권한 없음
                    - **404**: alias 또는 Task를 찾을 수 없음

                    ### 사용 예시
                    - 사용자가 "리액트"를 제안
                    - 관리자가 기존 Task "React"와 연결
                    - 이후 "리액트" 검색 시 "React" Task 반환
                    """
    )
    @PutMapping("/aliases/pending/{aliasId}/link")
    public RsData<TaskAliasDetailDto> linkPendingAlias(
            @PathVariable Long aliasId,
            @Valid @RequestBody LinkPendingAliasRequest request
    ) {
        validateAdminRole();

        TaskAlias linkedAlias = taskService.linkPendingAlias(aliasId, request.taskId());

        return new RsData<>(
                "200",
                "Pending Alias를 Task와 연결 성공",
                new TaskAliasDetailDto(linkedAlias)
        );
    }

    @Operation(
            summary = "pending alias를 새로운 표준 Task로 승격 (관리자)",
            description = """
                    ### 개요
                    관리자가 pending alias를 검토하여
                    새로운 표준 Task로 생성하고 연결합니다.

                    ### 권한
                    - **관리자 권한(ADMIN) 필수**

                    ### 처리 로직
                    1. aliasId로 TaskAlias 조회 및 pending 상태 확인
                    2. 동일 이름의 Task가 이미 존재하는지 확인
                    3. 새 Task 생성 (alias 이름으로)
                    4. TaskAlias를 새 Task와 연결

                    ### 응답 정보
                    - id: 생성된 Task ID
                    - name: Task 이름 (alias 이름과 동일)

                    ### 응답 코드
                    - **201**: Task 생성 및 연결 성공
                    - **400**: 이미 연결된 alias 또는 동일 이름 Task 존재
                    - **401**: 인증 필요
                    - **403**: 관리자 권한 없음
                    - **404**: alias를 찾을 수 없음

                    ### 사용 예시
                    - 사용자가 "Bun"을 제안
                    - 관리자가 새로운 기술로 판단하여 Task 승격
                    - 이후 "Bun"은 표준 Task로 검색 가능
                    """
    )
    @PostMapping("/aliases/pending/{aliasId}")
    public RsData<TaskDto> createTaskFromPending(
            @PathVariable Long aliasId
    ) {
        validateAdminRole();

        Task newTask = taskService.createTaskFromPending(aliasId);

        return new RsData<>(
                "201",
                "Pending Alias를 새로운 Task로 등록 성공",
                new TaskDto(newTask)
        );
    }

    @Operation(
            summary = "pending alias 삭제 (관리자)",
            description = """
                    ### 개요
                    관리자가 pending alias를 검토하여
                    부적절하거나 불필요한 제안을 삭제합니다.

                    ### 권한
                    - **관리자 권한(ADMIN) 필수**

                    ### 삭제 사유 예시
                    - 오타나 의미 없는 제안
                    - 중복 제안
                    - 부적절한 내용

                    ### 응답 코드
                    - **200**: 삭제 성공
                    - **400**: 이미 연결된 alias (pending 아님)
                    - **401**: 인증 필요
                    - **403**: 관리자 권한 없음
                    - **404**: alias를 찾을 수 없음

                    ### 참고
                    - pending 상태가 아닌 alias는 삭제 불가
                    - 이미 Task와 연결된 alias는 Task 자체를 삭제해야 함
                    """
    )
    @DeleteMapping("/aliases/pending/{aliasId}")
    public RsData<Void> deletePendingAlias(@PathVariable Long aliasId) {
        validateAdminRole();
        taskService.deletePendingAlias(aliasId);
        return new RsData<>("200", "Pending Alias 삭제 성공", null);
    }

    private void validateAdminRole() {
        Member member = rq.getActor();
        if(member == null) {
            throw new ServiceException("401", "로그인 후 이용해주세요.");
        }
        if(member.getRole() != Member.Role.ADMIN){
            throw new ServiceException("403", "권한이 없습니다.");
        }
    }
}
