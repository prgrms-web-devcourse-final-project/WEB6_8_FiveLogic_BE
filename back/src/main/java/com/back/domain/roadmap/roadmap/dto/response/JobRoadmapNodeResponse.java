package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record JobRoadmapNodeResponse(
    Long id,
    Long parentId,       // 부모 노드 ID (null이면 루트 노드)
    List<Long> childIds, // 자식 노드 ID 목록 (프론트엔드 렌더링용)
    Long taskId,         // Task와 연결된 경우의 표준 Task ID
    String taskName,     // 표시용 Task 이름
    String learningAdvice,       // 학습 조언/방법 (통합)
    String recommendedResources, // 추천 자료 (통합)
    String learningGoals,        // 학습 목표 (통합)
    Double difficulty,           // 난이도 (1-5, 평균)
    Double importance,           // 중요도 (1-5, 평균)
    Integer estimatedHours,      // 총 예상 학습 시간 (평균)
    int stepOrder,
    int level,           // 트리 깊이 (0: 루트, 1: 1단계 자식...)
    boolean isLinkedToTask,
    Double weight,       // 이 노드의 가중치 (JobRoadmapNodeStat에서)

    // 멘토 커버리지 통계
    Integer mentorCount,          // 이 노드를 선택한 멘토 수
    Integer totalMentorCount,     // 해당 직업의 전체 멘토 수
    Double mentorCoverageRatio,   // mentorCount / totalMentorCount (0.0 ~ 1.0)

    // UI 강조 표시용 (동적 계산)
    Boolean isEssential,          // 필수 노드 여부 (50% 이상)
    String essentialLevel,        // "CORE" (80%+) | "COMMON" (50%+) | "OPTIONAL" (<50%)

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<JobRoadmapNodeResponse> children
) {

    // 필수 경로 판정 기준 (상수)
    private static final double ESSENTIAL_THRESHOLD = 0.5;  // 50%
    private static final double CORE_THRESHOLD = 0.8;       // 80%

    // 정적 팩토리 메서드 - RoadmapNode로부터 Response DTO 생성 (자식 노드 정보 포함)
    public static JobRoadmapNodeResponse from(RoadmapNode node, List<JobRoadmapNodeResponse> children) {
        List<Long> childIds = children != null ?
            children.stream().map(JobRoadmapNodeResponse::id).toList() :
            List.of();

        return new JobRoadmapNodeResponse(
            node.getId(),
            node.getParent() != null ? node.getParent().getId() : null,
            childIds,
            node.getTask() != null ? node.getTask().getId() : null,
            node.getTask() != null ? node.getTask().getName() : node.getTaskName(),
            node.getLearningAdvice(),
            node.getRecommendedResources(),
            node.getLearningGoals(),
            node.getDifficulty() != null ? node.getDifficulty().doubleValue() : null,
            node.getImportance() != null ? node.getImportance().doubleValue() : null,
            node.getEstimatedHours(),
            node.getStepOrder(),
            node.getLevel(),
            node.getTask() != null,
            null, // weight는 서비스에서 별도로 설정
            null, // mentorCount는 서비스에서 별도로 설정
            null, // totalMentorCount는 서비스에서 별도로 설정
            null, // mentorCoverageRatio는 서비스에서 별도로 설정
            null, // isEssential은 서비스에서 별도로 설정
            null, // essentialLevel은 서비스에서 별도로 설정
            children != null ? children : List.of()
        );
    }

    // 가중치 설정 헬퍼 메서드 (불변 객체이므로 새 인스턴스 반환)
    public JobRoadmapNodeResponse withWeight(Double weight) {
        return new JobRoadmapNodeResponse(
            this.id, this.parentId, this.childIds, this.taskId, this.taskName,
            this.learningAdvice, this.recommendedResources, this.learningGoals,
            this.difficulty, this.importance, this.estimatedHours,
            this.stepOrder, this.level, this.isLinkedToTask, weight,
            this.mentorCount, this.totalMentorCount, this.mentorCoverageRatio,
            this.isEssential, this.essentialLevel,
            this.children
        );
    }

    // 통계 정보 설정 헬퍼 메서드 (JobRoadmapNodeStat으로부터 통계 추가)
    public JobRoadmapNodeResponse withStats(Integer mentorCount, Integer totalMentorCount, Double mentorCoverageRatio) {
        // 동적 계산: isEssential, essentialLevel
        Boolean isEssential = mentorCoverageRatio != null && mentorCoverageRatio >= ESSENTIAL_THRESHOLD;
        String essentialLevel = calculateEssentialLevel(mentorCoverageRatio);

        return new JobRoadmapNodeResponse(
            this.id, this.parentId, this.childIds, this.taskId, this.taskName,
            this.learningAdvice, this.recommendedResources, this.learningGoals,
            this.difficulty, this.importance, this.estimatedHours,
            this.stepOrder, this.level, this.isLinkedToTask, this.weight,
            mentorCount, totalMentorCount, mentorCoverageRatio,
            isEssential, essentialLevel,
            this.children
        );
    }

    // 필수 경로 레벨 계산 (동적)
    private static String calculateEssentialLevel(Double ratio) {
        if (ratio == null) return "UNKNOWN";
        if (ratio >= CORE_THRESHOLD) return "CORE";      // 80%+ : 핵심 필수
        if (ratio >= ESSENTIAL_THRESHOLD) return "COMMON"; // 50%+ : 일반 필수
        return "OPTIONAL";                                // 50%- : 선택
    }
}