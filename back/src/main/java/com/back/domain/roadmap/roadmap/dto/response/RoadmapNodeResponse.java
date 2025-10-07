package com.back.domain.roadmap.roadmap.dto.response;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;

public record RoadmapNodeResponse(
    Long id,
    Long taskId,         // Task와 연결된 경우의 표준 Task ID
    String taskName,     // 표시용 Task 이름(Task와 연결된 경우 해당 Task 이름, 자유 입력시 입력값)
    String learningAdvice,       // 학습 조언/방법
    String recommendedResources, // 추천 자료
    String learningGoals,        // 학습 목표
    Integer difficulty,          // 난이도 (1-5)
    Integer importance,          // 중요도 (1-5)
    Integer hoursPerDay,         // 하루 학습 시간
    Integer weeks,               // 학습 주차
    Integer estimatedHours,      // 총 예상 학습 시간
    int stepOrder,
    boolean isLinkedToTask // Task와 연결 여부
) {

    // 정적 팩터리 메서드 - RoadmapNode로부터 Response DTO 생성
    public static RoadmapNodeResponse from(RoadmapNode node) {
        return new RoadmapNodeResponse(
            node.getId(),
            node.getTask() != null ? node.getTask().getId() : null,
            node.getTaskName(), // taskName 필드 직접 사용 (Task 엔티티 접근 불필요)
            node.getLearningAdvice(),
            node.getRecommendedResources(),
            node.getLearningGoals(),
            node.getDifficulty(),
            node.getImportance(),
            node.getHoursPerDay(),
            node.getWeeks(),
            node.getEstimatedHours(),
            node.getStepOrder(),
            node.getTask() != null
        );
    }
}