package com.back.domain.roadmap.roadmap.repository;

import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {

    // 특정 로드맵의 특정 타입 노드들 삭제
    @Modifying
    @Query("DELETE FROM RoadmapNode r WHERE r.roadmapId = :roadmapId AND r.roadmapType = :roadmapType")
    void deleteByRoadmapIdAndRoadmapType(
        @Param("roadmapId") Long roadmapId,
        @Param("roadmapType") RoadmapNode.RoadmapType roadmapType
    );

    // 조회용 메서드 (성능 최적화용)
    List<RoadmapNode> findByRoadmapIdAndRoadmapTypeOrderByStepOrder(
        Long roadmapId,
        RoadmapNode.RoadmapType roadmapType
    );
}