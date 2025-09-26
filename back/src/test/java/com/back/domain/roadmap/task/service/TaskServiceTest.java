package com.back.domain.roadmap.task.service;

import com.back.domain.roadmap.task.dto.TaskAliasDto;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.entity.TaskAlias;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TaskServiceTest {
    @Autowired
    private TaskService taskService;

    @Test
    @DisplayName("Task 생성 - 성공")
    void t1() {
        // Given
        String taskName = "test task";

        // When
        Task createdTask = taskService.create(taskName);

        // Then
        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getId()).isNotNull();
        assertThat(createdTask.getName()).isEqualTo(taskName);
    }

    @Test
    @DisplayName("TaskAlias 생성 - 성공")
    void t2() {
        // Given
        Task task = taskService.create("test task");
        String aliasName = "test alias";

        // When
        TaskAlias createdAlias = taskService.createAlias(task, aliasName);

        // Then
        assertThat(createdAlias).isNotNull();
        assertThat(createdAlias.getId()).isNotNull();
        assertThat(createdAlias.getName()).isEqualTo(aliasName);
        assertThat(createdAlias.getTask()).isEqualTo(task);
        assertThat(createdAlias.getTask().getId()).isEqualTo(task.getId());
    }

    @Test
    @DisplayName("키워드 검색 - Task 이름으로 검색")
    void t3() {
        // Given
        String keyword = "Java";

        // When
        List<Task> results = taskService.searchByKeyword(keyword);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(task -> task.getName().contains(keyword));
    }

    @Test
    @DisplayName("키워드 검색 - TaskAlias로 검색")
    void t4() {
        // Given
        String keyword = "자바"; // initData에서 Java의 별칭으로 설정됨

        // When
        List<Task> results = taskService.searchByKeyword(keyword);

        // Then
        assertThat(results).isNotEmpty();
        // 자바라는 별칭을 가진 Task가 검색되어야 함
    }

    @Test
    @DisplayName("키워드 검색 - 결과 없음")
    void t5() {
        // Given
        String keyword = "존재하지 않는 기술";

        // When
        List<Task> results = taskService.searchByKeyword(keyword);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Pending Alias 생성 - 성공")
    void t6() {
        // Given
        String aliasName = "new task";

        // When
        TaskAlias pendingAlias = taskService.createPendingAlias(aliasName);

        // Then
        assertThat(pendingAlias).isNotNull();
        assertThat(pendingAlias.getId()).isNotNull();
        assertThat(pendingAlias.getName()).isEqualTo(aliasName);
        assertThat(pendingAlias.getTask()).isNull(); // pending 상태
    }

    @Test
    @DisplayName("Pending Alias 생성 실패 - 이미 등록된 Task")
    void t7() {
        // Given
        String existingTaskName = "Java"; // initData에 존재

        // When & Then
        assertThatThrownBy(() -> taskService.createPendingAlias(existingTaskName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400 : 이미 등록된 Task명입니다.");
    }

    @Test
    @DisplayName("Pending Alias 생성 실패 - 이미 등록된 TaskAlias")
    void t8() {
        // Given
        String existingAliasName = "자바"; // initData에 존재

        // When & Then
        assertThatThrownBy(() -> taskService.createPendingAlias(existingAliasName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400 : 이미 등록된 Task의 별칭입니다.");
    }

    @Test
    @DisplayName("Pending Alias 생성 실패 - 이미 제안된 Pending Alias")
    void t9() {
        // Given
        String aliasName = "중복 테스트";
        taskService.createPendingAlias(aliasName);

        // When & Then
        assertThatThrownBy(() -> taskService.createPendingAlias(aliasName))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400 : 이미 제안된 Task명입니다.");
    }

    @Test
    @DisplayName("Pending Alias 목록 조회 - 페이징")
    void t10() {
        // Given
        taskService.createPendingAlias("test1");
        taskService.createPendingAlias("test2");
        taskService.createPendingAlias("test3");

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<TaskAliasDto> result = taskService.getPendingTaskAliases(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);
        assertThat(result.getContent()).allMatch(dto -> dto.aliasId() != null);
        assertThat(result.getContent()).allMatch(dto -> dto.aliasName() != null);
    }

    @Test
    @DisplayName("Pending Alias를 기존 Task와 연결 - 성공")
    void t11() {
        // Given
        Task existingTask = taskService.create("기존 task");
        TaskAlias pendingAlias = taskService.createPendingAlias("연결 테스트");

        // When
        TaskAlias linkedAlias = taskService.linkPendingAlias(pendingAlias.getId(), existingTask.getId());

        // Then
        assertThat(linkedAlias).isNotNull();
        assertThat(linkedAlias.getTask()).isNotNull();
        assertThat(linkedAlias.getTask().getId()).isEqualTo(existingTask.getId());
        assertThat(linkedAlias.getTask().getName()).isEqualTo("기존 task");
        assertThat(linkedAlias.getName()).isEqualTo("연결 테스트");
    }

    @Test
    @DisplayName("Pending Alias를 기존 Task와 연결 실패 - 존재하지 않는 Alias")
    void t12() {
        // Given
        Long nonExistentAliasId = 99999L;
        Task existingTask = taskService.create("기존 task");

        // When & Then
        assertThatThrownBy(() -> taskService.linkPendingAlias(nonExistentAliasId, existingTask.getId()))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404 : 해당 별칭이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Pending Alias를 기존 Task와 연결 실패 - 존재하지 않는 Task")
    void t13() {
        // Given
        TaskAlias pendingAlias = taskService.createPendingAlias("연결 테스트");
        Long nonExistentTaskId = 99999L;

        // When & Then
        assertThatThrownBy(() -> taskService.linkPendingAlias(pendingAlias.getId(), nonExistentTaskId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404 : 해당 Task가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Pending Alias를 기존 Task와 연결 실패 - 이미 연결된 Alias")
    void t14() {
        // Given
        Task existingTask = taskService.create("기존 task");
        TaskAlias pendingAlias = taskService.createPendingAlias("연결 테스트");
        taskService.linkPendingAlias(pendingAlias.getId(), existingTask.getId()); // 먼저 연결

        Task anotherTask = taskService.create("다른 task");

        // When & Then
        assertThatThrownBy(() -> taskService.linkPendingAlias(pendingAlias.getId(), anotherTask.getId()))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400 : 이미 연결된 별칭입니다.");
    }

    @Test
    @DisplayName("Pending Alias를 새로운 Task로 생성 - 성공")
    void t15() {
        // Given
        TaskAlias pendingAlias = taskService.createPendingAlias("new task");

        // When
        Task createdTask = taskService.createTaskFromPending(pendingAlias.getId());

        // Then
        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getId()).isNotNull();
        assertThat(createdTask.getName()).isEqualTo("new task");
    }

    @Test
    @DisplayName("Pending Alias를 새로운 Task로 생성 실패 - 존재하지 않는 Alias")
    void t16() {
        // Given
        Long nonExistentAliasId = 99999L;

        // When & Then
        assertThatThrownBy(() -> taskService.createTaskFromPending(nonExistentAliasId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404 : 해당 별칭이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Pending Alias를 새로운 Task로 생성 실패 - 이미 연결된 Alias")
    void t17() {
        // Given
        Task existingTask = taskService.create("기존 task");
        TaskAlias pendingAlias = taskService.createPendingAlias("연결 테스트");
        taskService.linkPendingAlias(pendingAlias.getId(), existingTask.getId()); // 먼저 연결

        // When & Then
        assertThatThrownBy(() -> taskService.createTaskFromPending(pendingAlias.getId()))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400 : 이미 연결된 별칭입니다.");
    }

    @Test
    @DisplayName("Pending Alias 삭제 - 성공")
    void t19() {
        // Given
        TaskAlias pendingAlias = taskService.createPendingAlias("삭제 테스트");

        // When & Then
        assertThatCode(() -> taskService.deletePendingAlias(pendingAlias.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Pending Alias 삭제 실패 - 존재하지 않는 Alias")
    void t20() {
        // Given
        Long nonExistentAliasId = 99999L;

        // When & Then
        assertThatThrownBy(() -> taskService.deletePendingAlias(nonExistentAliasId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("404 : 해당 별칭이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Pending Alias 삭제 실패 - 연결된 Alias는 삭제 불가")
    void t21() {
        // Given
        Task existingTask = taskService.create("기존 task");
        TaskAlias pendingAlias = taskService.createPendingAlias("연결 테스트");
        taskService.linkPendingAlias(pendingAlias.getId(), existingTask.getId()); // 연결

        // When & Then
        assertThatThrownBy(() -> taskService.deletePendingAlias(pendingAlias.getId()))
                .isInstanceOf(ServiceException.class)
                .hasMessage("400 : 이미 연결된 별칭입니다.");
    }

    @Test
    @DisplayName("Task 개수 조회")
    void t22() {
        // Given
        long initialCount = taskService.count();
        taskService.create("count test1");
        taskService.create("count test2");

        // When
        long currentCount = taskService.count();

        // Then
        assertThat(currentCount).isEqualTo(initialCount + 2);
    }

    @Test
    @DisplayName("키워드 검색 - 대소문자 무시")
    void t23() {
        // Given
        String keyword = "JAVA"; // 대문자로 검색

        // When
        List<Task> results = taskService.searchByKeyword(keyword);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(task ->
                task.getName().toLowerCase().contains(keyword.toLowerCase()));
    }

    @Test
    @DisplayName("키워드 검색 - 부분 문자열 매칭")
    void t24() {
        // Given
        String keyword = "Sp"; // "Spring"의 일부

        // When
        List<Task> results = taskService.searchByKeyword(keyword);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getName()).isEqualTo("Spring Boot");

    }
}