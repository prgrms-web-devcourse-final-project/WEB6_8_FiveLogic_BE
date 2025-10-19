package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.response.TextFieldIntegrationResponse;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.task.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoadmapTreeBuilderTest {

    @Mock
    private TextFieldIntegrationService textFieldIntegrationService;

    @InjectMocks
    private RoadmapTreeBuilder roadmapTreeBuilder;

    private Map<Long, Task> taskMap;
    private Task taskJava, taskSpring, taskJpa, taskDocker, taskMysql;

    @BeforeEach
    void setUp() {
        taskJava = createTask(1L, "Java");
        taskSpring = createTask(2L, "Spring");
        taskJpa = createTask(3L, "JPA");
        taskDocker = createTask(4L, "Docker");
        taskMysql = createTask(5L, "MySQL");

        taskMap = Map.of(
                1L, taskJava,
                2L, taskSpring,
                3L, taskJpa,
                4L, taskDocker,
                5L, taskMysql
        );

        // 기본 stubbing: 빈 응답 반환 (lenient로 설정하여 override 가능)
        lenient().when(textFieldIntegrationService.integrateBatch(any())).thenReturn(new HashMap<>());
    }

    @Test
    @DisplayName("복잡한 통계 데이터를 기반으로 올바른 로드맵 트리를 생성한다.")
    void build_complexScenario_constructsCorrectTree() {
        // given
        RoadmapAggregator.AggregationResult aggregation = createComplexAggregationResult();
        mockTextFieldIntegration();

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        // 1. 루트 노드 검증
        assertThat(result.getRootKey()).isEqualTo("T:1"); // Java가 루트여야 함
        RoadmapNode root = result.getKeyToNode().get("T:1");
        assertThat(root).isNotNull();
        assertThat(root.getTaskName()).isEqualTo("Java");
        assertThat(root.getLevel()).isEqualTo(0);
        assertThat(root.getStepOrder()).isEqualTo(1);
        assertThat(root.getDifficulty()).isEqualTo(3); // (2+3)/2 = 2.5 -> 3
        assertThat(root.getImportance()).isEqualTo(5); // (5+4)/2 = 4.5 -> 5

        // 2. 생성된 노드 속성 검증 (AI 통합 필드)
        assertThat(root.getLearningAdvice()).isEqualTo("Integrated Java Advice");
        assertThat(root.getRecommendedResources()).isEqualTo("Integrated Java Resources");

        // 3. 트리 구조 검증 (BFS 결과)
        // Java(T:1)의 자식은 Spring(T:2)과 JPA(T:3)여야 함
        assertThat(root.getChildren()).hasSize(2);
        List<String> childrenNames = root.getChildren().stream()
                .map(RoadmapNode::getTaskName)
                .collect(Collectors.toList());
        assertThat(childrenNames).containsExactlyInAnyOrder("Spring", "JPA");

        RoadmapNode springNode = findChildByName(root, "Spring");
        RoadmapNode jpaNode = findChildByName(root, "JPA");

        assertThat(springNode).isNotNull();
        assertThat(springNode.getLevel()).isEqualTo(1);
        assertThat(springNode.getParent()).isEqualTo(root);

        assertThat(jpaNode).isNotNull();
        assertThat(jpaNode.getLevel()).isEqualTo(1);
        assertThat(jpaNode.getParent()).isEqualTo(root);

        // Spring(T:2)의 자식은 Docker(T:4)여야 함
        assertThat(springNode.getChildren()).hasSize(1);
        RoadmapNode dockerNode = springNode.getChildren().get(0);
        assertThat(dockerNode.getTaskName()).isEqualTo("Docker");
        assertThat(dockerNode.getLevel()).isEqualTo(2);
        assertThat(dockerNode.getParent()).isEqualTo(springNode);

        // 4. 순환 및 최적 부모 로직 검증
        // JPA(T:3)는 Spring(T:2)으로 가는 전이가 있지만, Spring의 최적 부모는 Java(T:1)이므로
        // JPA는 Spring을 자식으로 가지면 안됨.
        assertThat(jpaNode.getChildren()).isEmpty();

        // 5. 방문하지 않은 노드 검증 (MySQL은 어디에도 연결되지 않음)
        assertThat(result.getVisited()).doesNotContain("T:5");
    }

    @Test
    @DisplayName("rootCount가 비어있을 경우 전체 노드 등장 빈도를 기반으로 루트를 선택한다.")
    void build_whenRootCountIsEmpty_selectsRootFromOverallCount() {
        // given
        RoadmapAggregator.AggregationResult aggregation = createComplexAggregationResult();
        aggregation.rootCount.clear(); // 루트 카운트 강제 클리어
        mockTextFieldIntegration();

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        // agg.count가 가장 높은 Java(4)가 루트가 되어야 함
        assertThat(result.getRootKey()).isEqualTo("T:1");
    }

    @Test
    @DisplayName("TextFieldIntegrationService 호출 실패 시 빈 텍스트로 트리를 생성한다.")
    void build_whenTextFieldIntegrationFails_proceedsWithEmptyTexts() {
        // given
        RoadmapAggregator.AggregationResult aggregation = createComplexAggregationResult();
        // AI 서비스 Mock이 예외를 던지도록 설정
        when(textFieldIntegrationService.integrateBatch(any()))
                .thenThrow(new RuntimeException("AI service is down"));

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        // 예외가 발생해도 빌드는 중단되지 않아야 함
        assertThat(result).isNotNull();
        RoadmapNode root = result.getKeyToNode().get("T:1");
        assertThat(root).isNotNull();

        // 텍스트 필드는 모두 null이어야 함
        assertThat(root.getLearningAdvice()).isNull();
        assertThat(root.getRecommendedResources()).isNull();
        assertThat(root.getLearningGoals()).isNull();
    }

    @Test
    @DisplayName("BRANCH_THRESHOLD(0.25) 미만의 전이는 자식으로 선택되지 않는다 (2번째 이후 자식부터 적용)")
    void build_whenTransitionBelowThreshold_excludesFromChildren() {
        // given
        RoadmapAggregator.AggregationResult aggregation = new RoadmapAggregator.AggregationResult(3);

        // Java가 10번 등장
        aggregation.agg.put("T:1", new RoadmapAggregator.AggregatedNode(taskJava, "Java"));
        aggregation.agg.get("T:1").count = 10;
        aggregation.agg.put("T:2", new RoadmapAggregator.AggregatedNode(taskSpring, "Spring"));
        aggregation.agg.get("T:2").count = 5;
        aggregation.agg.put("T:3", new RoadmapAggregator.AggregatedNode(taskJpa, "JPA"));
        aggregation.agg.get("T:3").count = 3;
        aggregation.agg.put("T:4", new RoadmapAggregator.AggregatedNode(taskDocker, "Docker"));
        aggregation.agg.get("T:4").count = 1;

        aggregation.rootCount.put("T:1", 3);

        // Java → Spring: 5번 (1순위, 50%, 무조건 포함)
        // Java → JPA: 3번 (2순위, 30% > 25%, 포함)
        // Java → Docker: 1번 (3순위, 10% < 25%, 제외되어야 함)
        aggregation.transitions.put("T:1", new HashMap<>(Map.of("T:2", 5, "T:3", 3, "T:4", 1)));

        aggregation.positions.put("T:1", List.of(1, 1, 1));
        aggregation.positions.put("T:2", List.of(2, 2, 2, 2, 2));
        aggregation.positions.put("T:3", List.of(2, 2, 2));
        aggregation.positions.put("T:4", List.of(3));

        aggregation.mentorAppearSet.put("T:1", Set.of(101L, 102L, 103L));
        aggregation.mentorAppearSet.put("T:2", Set.of(101L, 102L, 103L));
        aggregation.mentorAppearSet.put("T:3", Set.of(101L, 102L));
        aggregation.mentorAppearSet.put("T:4", Set.of(101L));

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        RoadmapNode root = result.getKeyToNode().get("T:1");
        assertThat(root.getChildren()).hasSize(2); // Spring, JPA만 포함

        List<String> childNames = root.getChildren().stream()
                .map(RoadmapNode::getTaskName)
                .collect(Collectors.toList());
        assertThat(childNames).containsExactlyInAnyOrder("Spring", "JPA");
        assertThat(childNames).doesNotContain("Docker");
    }

    @Test
    @DisplayName("MAX_CHILDREN(4) 제한으로 상위 4개 자식만 선택된다.")
    void build_whenMoreThanMaxChildren_selectsTop4() {
        // given
        Task taskReact = createTask(6L, "React");
        Task taskVue = createTask(7L, "Vue");

        // 확장된 taskMap 생성
        Map<Long, Task> extendedTaskMap = new HashMap<>();
        extendedTaskMap.put(1L, taskJava);
        extendedTaskMap.put(2L, taskSpring);
        extendedTaskMap.put(3L, taskJpa);
        extendedTaskMap.put(4L, taskDocker);
        extendedTaskMap.put(5L, taskMysql);
        extendedTaskMap.put(6L, taskReact);
        extendedTaskMap.put(7L, taskVue);

        RoadmapAggregator.AggregationResult aggregation = new RoadmapAggregator.AggregationResult(3);
        aggregation.agg.put("T:1", new RoadmapAggregator.AggregatedNode(taskJava, "Java"));
        aggregation.agg.get("T:1").count = 10;
        aggregation.agg.put("T:2", new RoadmapAggregator.AggregatedNode(taskSpring, "Spring"));
        aggregation.agg.get("T:2").count = 10;
        aggregation.agg.put("T:3", new RoadmapAggregator.AggregatedNode(taskJpa, "JPA"));
        aggregation.agg.get("T:3").count = 8;
        aggregation.agg.put("T:4", new RoadmapAggregator.AggregatedNode(taskDocker, "Docker"));
        aggregation.agg.get("T:4").count = 6;
        aggregation.agg.put("T:5", new RoadmapAggregator.AggregatedNode(taskMysql, "MySQL"));
        aggregation.agg.get("T:5").count = 4;
        aggregation.agg.put("T:6", new RoadmapAggregator.AggregatedNode(taskReact, "React"));
        aggregation.agg.get("T:6").count = 2;
        aggregation.agg.put("T:7", new RoadmapAggregator.AggregatedNode(taskVue, "Vue"));
        aggregation.agg.get("T:7").count = 1;

        aggregation.rootCount.put("T:1", 3);

        // Java → 6개 자식 (모두 50% 이상 전이)
        aggregation.transitions.put("T:1", new HashMap<>(Map.of(
                "T:2", 10,  // 1순위
                "T:3", 8,   // 2순위
                "T:4", 6,   // 3순위
                "T:5", 4,   // 4순위
                "T:6", 2,   // 5순위 (제외되어야 함)
                "T:7", 1    // 6순위 (제외되어야 함)
        )));

        aggregation.positions.put("T:1", List.of(1, 1, 1));
        for (int i = 2; i <= 7; i++) {
            aggregation.positions.put("T:" + i, List.of(2, 2));
        }

        aggregation.mentorAppearSet.put("T:1", Set.of(101L, 102L, 103L));
        for (int i = 2; i <= 7; i++) {
            aggregation.mentorAppearSet.put("T:" + i, Set.of(101L, 102L));
        }

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, extendedTaskMap);

        // then
        RoadmapNode root = result.getKeyToNode().get("T:1");
        assertThat(root.getChildren()).hasSize(4); // 최대 4개만

        List<String> childNames = root.getChildren().stream()
                .map(RoadmapNode::getTaskName)
                .collect(Collectors.toList());

        // 상위 4개만 포함되어야 함
        assertThat(childNames).containsExactlyInAnyOrder("Spring", "JPA", "Docker", "MySQL");
        assertThat(childNames).doesNotContain("React", "Vue");
    }

    @Test
    @DisplayName("MAX_DEPTH(10) 초과 시 노드 추가를 중단한다.")
    void build_whenExceedingMaxDepth_stopsAddingNodes() {
        // given
        RoadmapAggregator.AggregationResult aggregation = new RoadmapAggregator.AggregationResult(3);
        Map<Long, Task> deepTaskMap = new HashMap<>();

        // 깊이 12까지의 선형 구조 생성 (MAX_DEPTH=10 하드코딩되어 있음)
        for (long i = 1; i <= 12; i++) {
            Task task = createTask(i, "Task" + i);
            deepTaskMap.put(i, task);
            aggregation.agg.put("T:" + i, new RoadmapAggregator.AggregatedNode(task, "Task" + i));
            aggregation.agg.get("T:" + i).count = (int) (13 - i);
            aggregation.positions.put("T:" + i, List.of((int) i));
            aggregation.mentorAppearSet.put("T:" + i, Set.of(101L, 102L));
        }

        aggregation.rootCount.put("T:1", 3);

        // 선형 전이: T1 → T2 → T3 → ... → T12
        for (int i = 1; i < 12; i++) {
            aggregation.transitions.put("T:" + i, new HashMap<>(Map.of("T:" + (i + 1), 10)));
        }

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, deepTaskMap);

        // then
        RoadmapNode root = result.getKeyToNode().get("T:1");
        assertThat(root).isNotNull();
        assertThat(root.getLevel()).isEqualTo(0);

        // 최대 깊이 확인 (level 0~9까지만, 총 10개 레벨)
        RoadmapNode current = root;
        int maxLevel = 0;
        while (!current.getChildren().isEmpty()) {
            current = current.getChildren().get(0);
            maxLevel = current.getLevel();
        }
        assertThat(maxLevel).isLessThan(10); // level 9가 최대

        // T11, T12는 방문되지 않아야 함
        assertThat(result.getVisited()).doesNotContain("T:11", "T:12");
    }

    @Test
    @DisplayName("복잡한 다중 분기 구조를 올바르게 생성한다.")
    void build_withComplexBranching_constructsCorrectTree() {
        // given
        RoadmapAggregator.AggregationResult aggregation = createMultiBranchAggregationResult();
        mockTextFieldIntegration();

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        // 루트 검증
        assertThat(result.getRootKey()).isEqualTo("T:1"); // Java
        RoadmapNode root = result.getKeyToNode().get("T:1");

        // Java는 2개 자식 (Spring, JPA)
        assertThat(root.getChildren()).hasSize(2);
        RoadmapNode springNode = findChildByName(root, "Spring");
        RoadmapNode jpaNode = findChildByName(root, "JPA");

        // Spring도 2개 자식 (Docker, MySQL)
        assertThat(springNode.getChildren()).hasSize(2);
        assertThat(findChildByName(springNode, "Docker")).isNotNull();
        assertThat(findChildByName(springNode, "MySQL")).isNotNull();

        // 모든 노드의 레벨이 올바른지 확인
        assertThat(root.getLevel()).isEqualTo(0);
        assertThat(springNode.getLevel()).isEqualTo(1);
        assertThat(jpaNode.getLevel()).isEqualTo(1);
        assertThat(findChildByName(springNode, "Docker").getLevel()).isEqualTo(2);
        assertThat(findChildByName(springNode, "MySQL").getLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deferred Retry 메커니즘이 올바르게 동작한다.")
    void build_withDeferredRetry_connectsNodesToBestParent() {
        // given
        RoadmapAggregator.AggregationResult aggregation = createDeferredRetryScenario();
        mockTextFieldIntegration();

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        // Docker의 best parent는 Spring이지만, BFS 순서상 Java에서 먼저 만남
        // Deferred Retry로 인해 Spring이 방문된 후 Docker가 Spring의 자식이 되어야 함
        RoadmapNode springNode = result.getKeyToNode().get("T:2");
        RoadmapNode dockerNode = result.getKeyToNode().get("T:4");

        assertThat(dockerNode.getParent()).isEqualTo(springNode);
        assertThat(springNode.getChildren()).contains(dockerNode);
    }

    @Test
    @DisplayName("순환 참조를 방지한다.")
    void build_withCircularReference_preventsLoop() {
        // given
        RoadmapAggregator.AggregationResult aggregation = new RoadmapAggregator.AggregationResult(3);

        aggregation.agg.put("T:1", new RoadmapAggregator.AggregatedNode(taskJava, "Java"));
        aggregation.agg.get("T:1").count = 3;
        aggregation.agg.put("T:2", new RoadmapAggregator.AggregatedNode(taskSpring, "Spring"));
        aggregation.agg.get("T:2").count = 3;
        aggregation.agg.put("T:3", new RoadmapAggregator.AggregatedNode(taskJpa, "JPA"));
        aggregation.agg.get("T:3").count = 2;

        aggregation.rootCount.put("T:1", 3);

        // 순환 구조: Java → Spring → JPA → Spring (순환!)
        aggregation.transitions.put("T:1", new HashMap<>(Map.of("T:2", 3)));
        aggregation.transitions.put("T:2", new HashMap<>(Map.of("T:3", 2)));
        aggregation.transitions.put("T:3", new HashMap<>(Map.of("T:2", 1))); // 순환 발생

        aggregation.positions.put("T:1", List.of(1, 1, 1));
        aggregation.positions.put("T:2", List.of(2, 2, 2));
        aggregation.positions.put("T:3", List.of(3, 3));

        aggregation.mentorAppearSet.put("T:1", Set.of(101L, 102L, 103L));
        aggregation.mentorAppearSet.put("T:2", Set.of(101L, 102L, 103L));
        aggregation.mentorAppearSet.put("T:3", Set.of(101L, 102L));

        mockTextFieldIntegration();

        // when
        RoadmapTreeBuilder.TreeBuildResult result = roadmapTreeBuilder.build(aggregation, taskMap);

        // then
        // Spring은 이미 방문되었으므로 JPA → Spring 전이는 alternative로 기록되어야 함
        RoadmapNode springNode = result.getKeyToNode().get("T:2");
        RoadmapNode jpaNode = result.getKeyToNode().get("T:3");

        // JPA의 자식에 Spring이 없어야 함 (순환 방지)
        assertThat(jpaNode.getChildren()).doesNotContain(springNode);

        // T:2(Spring)가 alternative로 기록되어야 함
        assertThat(result.getSkippedParents().get("T:2")).isNotEmpty();
    }


    // --- Helper Methods ---

    private RoadmapAggregator.AggregationResult createMultiBranchAggregationResult() {
        RoadmapAggregator.AggregationResult result = new RoadmapAggregator.AggregationResult(4);

        // Java (루트)
        result.agg.put("T:1", new RoadmapAggregator.AggregatedNode(taskJava, "Java"));
        result.agg.get("T:1").count = 4;

        // Spring, JPA (Java의 자식)
        result.agg.put("T:2", new RoadmapAggregator.AggregatedNode(taskSpring, "Spring"));
        result.agg.get("T:2").count = 4;
        result.agg.put("T:3", new RoadmapAggregator.AggregatedNode(taskJpa, "JPA"));
        result.agg.get("T:3").count = 3;

        // Docker, MySQL (Spring의 자식)
        result.agg.put("T:4", new RoadmapAggregator.AggregatedNode(taskDocker, "Docker"));
        result.agg.get("T:4").count = 2;
        result.agg.put("T:5", new RoadmapAggregator.AggregatedNode(taskMysql, "MySQL"));
        result.agg.get("T:5").count = 2;

        result.rootCount.put("T:1", 4);

        // 전이: Java → Spring/JPA, Spring → Docker/MySQL
        result.transitions.put("T:1", new HashMap<>(Map.of("T:2", 4, "T:3", 3)));
        result.transitions.put("T:2", new HashMap<>(Map.of("T:4", 2, "T:5", 2)));

        result.positions.put("T:1", List.of(1, 1, 1, 1));
        result.positions.put("T:2", List.of(2, 2, 2, 2));
        result.positions.put("T:3", List.of(2, 2, 2));
        result.positions.put("T:4", List.of(3, 3));
        result.positions.put("T:5", List.of(3, 3));

        result.mentorAppearSet.put("T:1", Set.of(101L, 102L, 103L, 104L));
        result.mentorAppearSet.put("T:2", Set.of(101L, 102L, 103L, 104L));
        result.mentorAppearSet.put("T:3", Set.of(101L, 102L, 103L));
        result.mentorAppearSet.put("T:4", Set.of(101L, 102L));
        result.mentorAppearSet.put("T:5", Set.of(101L, 102L));

        return result;
    }

    private RoadmapAggregator.AggregationResult createDeferredRetryScenario() {
        RoadmapAggregator.AggregationResult result = new RoadmapAggregator.AggregationResult(3);

        result.agg.put("T:1", new RoadmapAggregator.AggregatedNode(taskJava, "Java"));
        result.agg.get("T:1").count = 3;
        result.agg.put("T:2", new RoadmapAggregator.AggregatedNode(taskSpring, "Spring"));
        result.agg.get("T:2").count = 3;
        result.agg.put("T:4", new RoadmapAggregator.AggregatedNode(taskDocker, "Docker"));
        result.agg.get("T:4").count = 2;

        result.rootCount.put("T:1", 3);

        // Java → Spring (3번), Java → Docker (1번)
        // Spring → Docker (3번) - Docker의 best parent는 Spring
        result.transitions.put("T:1", new HashMap<>(Map.of("T:2", 3, "T:4", 1)));
        result.transitions.put("T:2", new HashMap<>(Map.of("T:4", 3)));

        result.positions.put("T:1", List.of(1, 1, 1));
        result.positions.put("T:2", List.of(2, 2, 2));
        result.positions.put("T:4", List.of(3, 3));

        result.mentorAppearSet.put("T:1", Set.of(101L, 102L, 103L));
        result.mentorAppearSet.put("T:2", Set.of(101L, 102L, 103L));
        result.mentorAppearSet.put("T:4", Set.of(101L, 102L));

        return result;
    }


    // --- Helper Methods ---

    private RoadmapAggregator.AggregationResult createComplexAggregationResult() {
        RoadmapAggregator.AggregationResult result = new RoadmapAggregator.AggregationResult(4);

        // 1. agg (노드 등장 횟수)
        result.agg.put("T:1", new RoadmapAggregator.AggregatedNode(taskJava, "Java"));
        result.agg.get("T:1").count = 4;
        result.agg.put("T:2", new RoadmapAggregator.AggregatedNode(taskSpring, "Spring"));
        result.agg.get("T:2").count = 3;
        result.agg.put("T:3", new RoadmapAggregator.AggregatedNode(taskJpa, "JPA"));
        result.agg.get("T:3").count = 2;
        result.agg.put("T:4", new RoadmapAggregator.AggregatedNode(taskDocker, "Docker"));
        result.agg.get("T:4").count = 1;
        result.agg.put("T:5", new RoadmapAggregator.AggregatedNode(taskMysql, "MySQL"));
        result.agg.get("T:5").count = 1;


        // 2. rootCount (루트 노드 빈도)
        result.rootCount.put("T:1", 3); // Java가 압도적인 루트
        result.rootCount.put("T:4", 1);

        // 3. transitions (전이)
        // Java -> Spring (3번)
        // Java -> JPA (2번)
        // Spring -> Docker (1번)
        // JPA -> Spring (1번) - 순환 구조 및 부모 경쟁 유발
        result.transitions.put("T:1", new HashMap<>(Map.of("T:2", 3, "T:3", 2)));
        result.transitions.put("T:2", new HashMap<>(Map.of("T:4", 1)));
        result.transitions.put("T:3", new HashMap<>(Map.of("T:2", 1)));


        // 4. positions (평균 위치)
        result.positions.put("T:1", List.of(1, 1, 1, 2)); // avg ~1.25
        result.positions.put("T:2", List.of(2, 2, 3));    // avg ~2.33
        result.positions.put("T:3", List.of(2, 3));       // avg ~2.5
        result.positions.put("T:4", List.of(3));          // avg 3.0

        // 5. mentorAppearSet (멘토 커버리지)
        result.mentorAppearSet.put("T:1", Set.of(101L, 102L, 103L, 104L)); // 4/4
        result.mentorAppearSet.put("T:2", Set.of(101L, 102L, 103L));       // 3/4
        result.mentorAppearSet.put("T:3", Set.of(101L, 102L));             // 2/4
        result.mentorAppearSet.put("T:4", Set.of(101L));                   // 1/4

        // 6. descriptions (상세 정보)
        result.descriptions.learningAdvices.put("T:1", List.of("Java advice 1", "Java advice 2"));
        result.descriptions.recommendedResources.put("T:1", List.of("Java resource 1"));
        result.descriptions.difficulties.put("T:1", List.of(2, 3)); // avg 2.5
        result.descriptions.importances.put("T:1", List.of(5, 4));  // avg 4.5
        result.descriptions.estimatedHours.put("T:2", List.of(40, 60, 80)); // avg 60

        return result;
    }

    private void mockTextFieldIntegration() {
        TextFieldIntegrationResponse javaResponse = new TextFieldIntegrationResponse(
                "Integrated Java Advice",
                "Integrated Java Resources",
                "Integrated Java Goals"
        );
        // AI 서비스 Mocking
        lenient().when(textFieldIntegrationService.integrateBatch(any()))
                .thenReturn(Map.of("T:1", javaResponse));
    }

    private Task createTask(Long id, String name) {
        Task task = new Task(name);
        try {
            Field idField = task.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    private RoadmapNode findChildByName(RoadmapNode parent, String name) {
        return parent.getChildren().stream()
                .filter(node -> name.equals(node.getTaskName()))
                .findFirst()
                .orElse(null);
    }
}
