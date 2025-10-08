package com.back.domain.roadmap.roadmap.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoadmapNodeRequest(
        Long taskId,  // nullable - Task와 연결되지 않은 경우 null

        @NotBlank(message = "Task 이름은 필수입니다.")
        @Size(max = 100, message = "Task 이름은 100자를 초과할 수 없습니다.")
        String taskName,  // 표시용 Task 이름 (rawTaskName으로 저장)

        @Size(max = 2000, message = "학습 조언은 2000자를 초과할 수 없습니다.")
        String learningAdvice,  // 학습 조언/방법

        @Size(max = 2000, message = "추천 자료는 2000자를 초과할 수 없습니다.")
        String recommendedResources,  // 추천 자료

        @Size(max = 1000, message = "학습 목표는 1000자를 초과할 수 없습니다.")
        String learningGoals,  // 학습 목표

        @Min(value = 1, message = "난이도는 1 이상이어야 합니다.")
        @Max(value = 5, message = "난이도는 5 이하이어야 합니다.")
        Integer difficulty,  // 난이도 (1-5)

        @Min(value = 1, message = "중요도는 1 이상이어야 합니다.")
        @Max(value = 5, message = "중요도는 5 이하이어야 합니다.")
        Integer importance,  // 중요도 (1-5)

        @Min(value = 1, message = "하루 학습 시간은 1 이상이어야 합니다.")
        Integer hoursPerDay,  // 하루 학습 시간 (시간 단위)

        @Min(value = 1, message = "학습 주차는 1 이상이어야 합니다.")
        Integer weeks,  // 학습 주차

        @Min(value = 1, message = "단계 순서는 1 이상이어야 합니다.")
        int stepOrder
) {}