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

    @Query("SELECT MAX(n.level) FROM RoadmapNode n " +
            "WHERE n.roadmapId = :roadmapId AND n.roadmapType = :roadmapType")
    Integer findMaxLevelByRoadmapIdAndRoadmapType(
            @Param("roadmapId") Long roadmapId,
            @Param("roadmapType") RoadmapNode.RoadmapType roadmapType);

    @Modifying
    @Query("DELETE FROM RoadmapNode n " +
            "WHERE n.roadmapId = :roadmapId " +
            "AND n.roadmapType = :roadmapType " +
            "AND n.level = :level")
    void deleteByRoadmapIdAndRoadmapTypeAndLevel(
            @Param("roadmapId") Long roadmapId,
            @Param("roadmapType") RoadmapNode.RoadmapType roadmapType,
            @Param("level") int level);

    // 부모-자식 구조를 가진 엔티티를 삭제하기 위해 자식부터 순서대로 삭제(PQL 2단계 방식)
    @Modifying
    @Query("DELETE FROM RoadmapNode r WHERE r.parent IS NOT NULL AND r.roadmapId = :roadmapId AND r.roadmapType = :roadmapType")
    void deleteChildren(@Param("roadmapId") Long roadmapId, @Param("roadmapType") RoadmapNode.RoadmapType roadmapType);

    @Modifying
    @Query("DELETE FROM RoadmapNode r WHERE r.parent IS NULL AND r.roadmapId = :roadmapId AND r.roadmapType = :roadmapType")
    void deleteParents(@Param("roadmapId") Long roadmapId, @Param("roadmapType") RoadmapNode.RoadmapType roadmapType);


    @Query("SELECT n.id FROM RoadmapNode n WHERE n.roadmapId = :roadmapId AND n.roadmapType = :roadmapType")
    List<Long> findIdsByRoadmapIdAndRoadmapType(@Param("roadmapId") Long roadmapId,
                                                @Param("roadmapType") RoadmapNode.RoadmapType roadmapType);

    // 조회용 메서드 (성능 최적화용)
    List<RoadmapNode> findByRoadmapIdAndRoadmapTypeOrderByStepOrder(
        Long roadmapId,
        RoadmapNode.RoadmapType roadmapType
    );
}