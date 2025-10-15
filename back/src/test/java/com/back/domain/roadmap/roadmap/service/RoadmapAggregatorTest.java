
package com.back.domain.roadmap.roadmap.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.task.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoadmapAggregatorTest {

    private RoadmapAggregator roadmapAggregator;

    // --- Mock Data ---
    private Mentor mentor1, mentor2, mentor3;
    private Task taskJava, taskSpring, taskJpa, taskDocker;

    @BeforeEach
    void setUp() {
        roadmapAggregator = new RoadmapAggregator();

        mentor1 = createMentor(101L);
        mentor2 = createMentor(102L);
        mentor3 = createMentor(103L);

        taskJava = createTask(1L, "Java");
        taskSpring = createTask(2L, "Spring");
        taskJpa = createTask(3L, "JPA");
        taskDocker = createTask(4L, "Docker");
    }

    @Test
    @DisplayName("다양한 멘토 로드맵을 집계하여 모든 필드의 통계를 정확하게 생성한다.")
    void aggregate_complexScenario_aggregatesAllFieldsCorrectly() {
        // given
        List<MentorRoadmap> mentorRoadmaps = createComplexMentorRoadmaps();

        // when
        RoadmapAggregator.AggregationResult result = roadmapAggregator.aggregate(mentorRoadmaps);

        // then
        assertThat(result.getTotalMentorCount()).isEqualTo(3);

        verifyAggregatedNodes(result.getAgg());
        verifyRootCandidates(result.getRootCount());
        verifyTransitions(result.getTransitions());
        verifyMentorAppearance(result.getMentorAppearSet());
        verifyNodePositions(result.getPositions());
        verifyDescriptionCollections(result.getDescriptions());
    }

    @Test
    @DisplayName("멘토 로드맵이 비어 있을 경우 빈 결과를 반환한다.")
    void aggregate_emptyList_returnsEmptyResult() {
        // when
        RoadmapAggregator.AggregationResult result = roadmapAggregator.aggregate(List.of());

        // then
        assertThat(result.getTotalMentorCount()).isEqualTo(0);
        assertThat(result.getAgg()).isEmpty();
        assertThat(result.getRootCount()).isEmpty();
        assertThat(result.getTransitions()).isEmpty();
        assertThat(result.getMentorAppearSet()).isEmpty();
    }

    @Test
    @DisplayName("노드가 비어 있는 멘토 로드맵은 통계에 반영되지 않는다.")
    void aggregate_emptyNodesInRoadmap_areIgnored() {
        // given
        MentorRoadmap emptyRoadmap = createMentorRoadmap(1L, mentor1, "빈 로드맵");
        // 노드를 추가하지 않음

        // when
        RoadmapAggregator.AggregationResult result = roadmapAggregator.aggregate(List.of(emptyRoadmap));

        // then
        assertThat(result.getTotalMentorCount()).isEqualTo(1);
        assertThat(result.getAgg()).isEmpty();
        assertThat(result.getRootCount()).isEmpty();
        assertThat(result.getTransitions()).isEmpty();
    }

    @Test
    @DisplayName("Task와 TaskName이 모두 없는 노드를 처리할 수 있다.")
    void aggregate_nodeWithNoTaskOrName_generatesUnknownKey() {
        // given
        MentorRoadmap roadmap = createMentorRoadmap(1L, mentor1, "멘토1 로드맵");
        RoadmapNode unknownNode = RoadmapNode.builder()
                .roadmapId(roadmap.getId())
                .roadmapType(RoadmapNode.RoadmapType.MENTOR)
                .stepOrder(1)
                .task(null)
                .taskName(null)
                .build();
        roadmap.addNodes(List.of(unknownNode));

        // when
        RoadmapAggregator.AggregationResult result = roadmapAggregator.aggregate(List.of(roadmap));

        // then
        assertThat(result.getAgg()).containsKey("N:__unknown__");
        assertThat(result.getAgg().get("N:__unknown__").count).isEqualTo(1);
        assertThat(result.getRootCount()).containsEntry("N:__unknown__", 1);
    }

    @Test
    @DisplayName("하나의 로드맵에서 동일 Task가 여러 번 등장할 경우 누적되어야 한다.")
    void aggregate_sameTaskMultipleTimes_inSingleRoadmap_countsAccumulated() {
        // given
        MentorRoadmap roadmap = createMentorRoadmap(1L, mentor1, "중복 Task 로드맵");
        RoadmapNode node1 = createStandardNode(roadmap.getId(), 1, taskJava, null, null, null, null, null, null);
        RoadmapNode node2 = createStandardNode(roadmap.getId(), 2, taskJava, null, null, null, null, null, null);
        roadmap.addNodes(List.of(node1, node2));

        // when
        RoadmapAggregator.AggregationResult result = roadmapAggregator.aggregate(List.of(roadmap));

        // then
        assertThat(result.getAgg()).containsKey("T:1");
        assertThat(result.getAgg().get("T:1").count).isEqualTo(2); // count 누적
        assertThat(result.getPositions().get("T:1")).containsExactlyInAnyOrder(1, 2);
        assertThat(result.getMentorAppearSet().get("T:1")).containsExactly(mentor1.getId());
    }

    @Test
    @DisplayName("여러 멘토가 동일한 전이(Transition)를 가질 경우 카운트가 누적된다.")
    void aggregate_duplicateTransitions_areCounted() {
        // given: 두 멘토가 모두 Java -> Spring 로드맵을 가짐
        MentorRoadmap roadmap1 = createMentorRoadmap(1L, mentor1, "로드맵1");
        roadmap1.addNodes(List.of(
                createStandardNode(roadmap1.getId(), 1, taskJava, null, null, null, null, null, null),
                createStandardNode(roadmap1.getId(), 2, taskSpring, null, null, null, null, null, null)
        ));

        MentorRoadmap roadmap2 = createMentorRoadmap(2L, mentor2, "로드맵2");
        roadmap2.addNodes(List.of(
                createStandardNode(roadmap2.getId(), 1, taskJava, null, null, null, null, null, null),
                createStandardNode(roadmap2.getId(), 2, taskSpring, null, null, null, null, null, null)
        ));

        // when
        RoadmapAggregator.AggregationResult result = roadmapAggregator.aggregate(List.of(roadmap1, roadmap2));

        // then
        assertThat(result.getTransitions().get("T:1")).containsEntry("T:2", 2);
        assertThat(result.getAgg().get("T:1").count).isEqualTo(2);
        assertThat(result.getAgg().get("T:2").count).isEqualTo(2);
    }

    private void verifyAggregatedNodes(Map<String, RoadmapAggregator.AggregatedNode> agg) {
        assertThat(agg).hasSize(5);
        assertThat(agg.get("T:1").count).isEqualTo(3);
        assertThat(agg.get("T:1").displayName).isEqualTo("Java");
        assertThat(agg.get("T:2").count).isEqualTo(2);
        assertThat(agg.get("T:2").displayName).isEqualTo("Spring");
        assertThat(agg.get("T:3").count).isEqualTo(1);
        assertThat(agg.get("T:3").displayName).isEqualTo("JPA");
        assertThat(agg.get("T:4").count).isEqualTo(1);
        assertThat(agg.get("T:4").displayName).isEqualTo("Docker");
        assertThat(agg.get("N:custom db task").count).isEqualTo(1);
        assertThat(agg.get("N:custom db task").displayName).isEqualTo("Custom DB Task");
    }

    private void verifyRootCandidates(Map<String, Integer> rootCount) {
        assertThat(rootCount).hasSize(2);
        assertThat(rootCount.get("T:1")).isEqualTo(2);
        assertThat(rootCount.get("T:4")).isEqualTo(1);
    }

    private void verifyTransitions(Map<String, Map<String, Integer>> transitions) {
        assertThat(transitions.get("T:1")).containsEntry("T:2", 1).containsEntry("T:3", 1);
        assertThat(transitions.get("T:2")).containsEntry("N:custom db task", 1);
        assertThat(transitions.get("T:3")).containsEntry("T:2", 1);
        assertThat(transitions.get("T:4")).containsEntry("T:1", 1);
    }

    private void verifyMentorAppearance(Map<String, java.util.Set<Long>> mentorAppearSet) {
        assertThat(mentorAppearSet.get("T:1")).containsExactlyInAnyOrder(101L, 102L, 103L);
        assertThat(mentorAppearSet.get("T:2")).containsExactlyInAnyOrder(101L, 102L);
        assertThat(mentorAppearSet.get("T:3")).containsExactlyInAnyOrder(102L);
        assertThat(mentorAppearSet.get("T:4")).containsExactlyInAnyOrder(103L);
        assertThat(mentorAppearSet.get("N:custom db task")).containsExactlyInAnyOrder(101L);
    }

    private void verifyNodePositions(Map<String, List<Integer>> positions) {
        assertThat(positions.get("T:1")).containsExactlyInAnyOrder(1, 1, 2);
        assertThat(positions.get("T:2")).containsExactlyInAnyOrder(2, 3);
        assertThat(positions.get("T:3")).containsExactlyInAnyOrder(2);
        assertThat(positions.get("T:4")).containsExactlyInAnyOrder(1);
        assertThat(positions.get("N:custom db task")).containsExactlyInAnyOrder(3);
    }

    private void verifyDescriptionCollections(RoadmapAggregator.DescriptionCollections descriptions) {
        assertThat(descriptions.getLearningAdvices().get("T:1"))
                .containsExactlyInAnyOrder("Java Advice from Mentor1", "Java Advice from Mentor2");
        assertThat(descriptions.getRecommendedResources().get("T:2"))
                .containsExactly("Spring Resource from Mentor1");
        assertThat(descriptions.getLearningGoals().get("T:1"))
                .containsExactly("Java Goal from Mentor2");
        assertThat(descriptions.getDifficulties().get("T:1"))
                .containsExactlyInAnyOrder(2, 3);
        assertThat(descriptions.getImportances().get("T:2"))
                .containsExactlyInAnyOrder(5, 4);
        assertThat(descriptions.getEstimatedHours().get("T:1"))
                .containsExactly(40);
    }

    // --- Helper Methods to build mock data (수정된 헬퍼) ---

    private List<MentorRoadmap> createComplexMentorRoadmaps() {
        // Mentor 1: Java -> Spring -> Custom DB Task
        MentorRoadmap roadmap1 = createMentorRoadmap(1L, mentor1, "멘토1 로드맵");
        RoadmapNode node1_1 = createStandardNode(roadmap1.getId(), 1, taskJava, "Java Advice from Mentor1", null, null, 2, 5, 40);
        RoadmapNode node1_2 = createStandardNode(roadmap1.getId(), 2, taskSpring, null, "Spring Resource from Mentor1", null, 4, 5, 80);
        RoadmapNode node1_3 = createCustomNode(roadmap1.getId(), 3, "Custom DB Task", null, null, null, 3, 3, 20);
        roadmap1.addNodes(Arrays.asList(node1_1, node1_2, node1_3));

        // Mentor 2: Java -> JPA -> Spring
        MentorRoadmap roadmap2 = createMentorRoadmap(2L, mentor2, "멘토2 로드맵");
        RoadmapNode node2_1 = createStandardNode(roadmap2.getId(), 1, taskJava, "Java Advice from Mentor2", null, "Java Goal from Mentor2", 3, 4, null);
        RoadmapNode node2_2 = createStandardNode(roadmap2.getId(), 2, taskJpa, null, null, "JPA Goal", 3, 5, 60);
        RoadmapNode node2_3 = createStandardNode(roadmap2.getId(), 3, taskSpring, null, null, null, 5, 4, 100);
        roadmap2.addNodes(Arrays.asList(node2_1, node2_2, node2_3));

        // Mentor 3: Docker -> Java
        MentorRoadmap roadmap3 = createMentorRoadmap(3L, mentor3, "멘토3 로드맵");
        RoadmapNode node3_1 = createStandardNode(roadmap3.getId(), 1, taskDocker, null, null, null, 2, 3, 20);
        RoadmapNode node3_2 = createStandardNode(roadmap3.getId(), 2, taskJava, null, null, null, null, null, null);
        roadmap3.addNodes(Arrays.asList(node3_1, node3_2));

        return Arrays.asList(roadmap1, roadmap2, roadmap3);
    }

    private Mentor createMentor(Long id) {
        Member member = new Member(id, "mentor" + id + "@test.com", "테스트멘토" + id, "테스트멘토" + id, Member.Role.MENTOR);
        Mentor mentor = Mentor.builder().member(member).job(null).build();
        try {
            Field idField = mentor.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mentor, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return mentor;
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

    private MentorRoadmap createMentorRoadmap(Long id, Mentor mentor, String title) {
        MentorRoadmap roadmap = new MentorRoadmap(mentor, title, "테스트 설명");
        try {
            Field idField = roadmap.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(roadmap, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return roadmap;
    }

    private RoadmapNode createStandardNode(Long roadmapId, int order, Task task, String advice, String resource, String goal, Integer difficulty, Integer importance, Integer estimatedHours) {
        return RoadmapNode.builder()
                .roadmapId(roadmapId) // ★★★★★ FIX: 소속될 로드맵 ID 설정
                .roadmapType(RoadmapNode.RoadmapType.MENTOR) // ★★★★★ FIX: 타입 명시
                .stepOrder(order)
                .task(task)
                .taskName(task.getName())
                .learningAdvice(advice)
                .recommendedResources(resource)
                .learningGoals(goal)
                .difficulty(difficulty)
                .importance(importance)
                .estimatedHours(estimatedHours)
                .build();
    }

    private RoadmapNode createCustomNode(Long roadmapId, int order, String taskName, String advice, String resource, String goal, Integer difficulty, Integer importance, Integer estimatedHours) {
        return RoadmapNode.builder()
                .roadmapId(roadmapId)
                .roadmapType(RoadmapNode.RoadmapType.MENTOR)
                .stepOrder(order)
                .task(null)
                .taskName(taskName)
                .learningAdvice(advice)
                .recommendedResources(resource)
                .learningGoals(goal)
                .difficulty(difficulty)
                .importance(importance)
                .estimatedHours(estimatedHours)
                .build();
    }
}
