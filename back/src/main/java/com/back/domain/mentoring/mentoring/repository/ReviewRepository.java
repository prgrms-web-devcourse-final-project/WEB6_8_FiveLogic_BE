package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservationId(Long id);

    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        INNER JOIN r.reservation res
        WHERE res.mentor = :mentor
        """)
    Double findAverageRating(
        @Param("mentor") Mentor mentor
    );

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.reservation.mentoring.id = :mentoringId
        ORDER BY r.createDate DESC
        """)
    Page<Review> findAllByMentoringId(
        @Param("mentoringId") Long mentoringId,
        Pageable pageable
    );
}
