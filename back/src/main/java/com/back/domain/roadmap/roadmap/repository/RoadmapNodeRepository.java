package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {

    /**
     * 멘토 로드맵의 모든 노드를 stepOrder 순으로 조회 (Task 정보 포함)
     * N+1 문제 방지를 위한 fetch join 사용
     */
    @Query("""
        SELECT rn FROM RoadmapNode rn
        LEFT JOIN FETCH rn.task t
        WHERE rn.roadmapId = :roadmapId
        AND rn.roadmapType = 'MENTOR'
        ORDER BY rn.stepOrder ASC
        """)
    List<RoadmapNode> findMentorRoadmapNodesWithTask(@Param("roadmapId") Long roadmapId);

    /**
     * 특정 로드맵의 노드 개수 조회
     */
    @Query("""
        SELECT COUNT(rn) FROM RoadmapNode rn
        WHERE rn.roadmapId = :roadmapId
        AND rn.roadmapType = :roadmapType
        """)
    long countByRoadmapIdAndType(@Param("roadmapId") Long roadmapId,
                               @Param("roadmapType") RoadmapNode.RoadmapType roadmapType);

    /**
     * 특정 로드맵의 마지막 stepOrder 조회 (새 노드 추가 시 사용)
     */
    @Query("""
        SELECT MAX(rn.stepOrder) FROM RoadmapNode rn
        WHERE rn.roadmapId = :roadmapId
        AND rn.roadmapType = 'MENTOR'
        """)
    Integer findMaxStepOrderByMentorRoadmap(@Param("roadmapId") Long roadmapId);

    /**
     * 로드맵 삭제 시 관련 노드들 일괄 삭제
     */
    void deleteByRoadmapIdAndRoadmapType(Long roadmapId, RoadmapNode.RoadmapType roadmapType);
}