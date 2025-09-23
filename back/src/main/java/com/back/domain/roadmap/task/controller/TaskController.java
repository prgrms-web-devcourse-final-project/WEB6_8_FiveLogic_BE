package com.back.domain.roadmap.task.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.roadmap.task.dto.*;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.entity.TaskAlias;
import com.back.domain.roadmap.task.service.TaskService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final Rq rq;

    // 검색 api
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

    // 사용자가 새로운 기술 제안 (pending alias 생성)
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
    // 나중에 CORS 설정
    @GetMapping("/aliases/pending")
    public RsData<Page<TaskAliasDto>> getPendingTaskAliases(
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Member member = rq.getActor();
        if(member == null) {
            return new RsData<>("401", "로그인 후 이용해주세요.");
        }
        if(member.getRole() != Member.Role.ADMIN){
            return new RsData<>("403", "권한이 없습니다.");
        }

        Page<TaskAliasDto> pendingTaskAliases = taskService.getPendingTaskAliases(pageable);

        return  new RsData<>(
                "200",
                "pending 상태의 TaskAlias 목록 조회 성공",
                pendingTaskAliases
        );
    }

    // Pending alias를 기존 Task와 연결
    @PutMapping("/aliases/pending/{aliasId}/link")
    public RsData<TaskAliasDetailDto> linkPendingAlias(
            @PathVariable Long aliasId,
            @Valid @RequestBody LinkPendingAliasRequest request
    ) {
        Member member = rq.getActor();
        if(member == null) {
            return new RsData<>("401", "로그인 후 이용해주세요.");
        }
        if(member.getRole() != Member.Role.ADMIN){
            return new RsData<>("403", "권한이 없습니다.");
        }

        TaskAlias linkedAlias = taskService.linkPendingAlias(aliasId, request.taskId());

        return new RsData<>(
                "200",
                "Pending Alias를 Task와 연결 성공",
                new TaskAliasDetailDto(linkedAlias)
        );
    }

    // Pending alias를 새로운 Task로 등록(생성)
    @PostMapping("/aliases/pending/{aliasId}")
    public RsData<TaskDto> createTaskFromPending(
            @PathVariable Long aliasId
    ) {
        Member member = rq.getActor();
        if(member == null) {
            return new RsData<>("401", "로그인 후 이용해주세요.");
        }
        if(member.getRole() != Member.Role.ADMIN){
            return new RsData<>("403", "권한이 없습니다.");
        }

        Task newTask = taskService.createTaskFromPending(aliasId);

        return new RsData<>(
                "201",
                "Pending Alias를 새로운 Task로 등록 성공",
                new TaskDto(newTask)
        );
    }
}
