package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservationId(Long reservationId);

    @Query("""
        SELECT ROUND(AVG(r.rating), 1)
        FROM Review r
        INNER JOIN r.reservation res
        WHERE res.mentor.id = :mentorId
        """)
    Double calculateMentorAverageRating(
        @Param("mentorId") Long mentorId
    );

    @Query("""
        SELECT ROUND(AVG(r.rating), 1)
        FROM Review r
        INNER JOIN r.reservation res
        WHERE res.mentoring.id = :mentoringId
        """)
    Double calculateMentoringAverageRating(
        @Param("mentoringId") Long mentoringId
    );

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.reservation.mentoring.id = :mentoringId
        """)
    Page<Review> findAllByMentoringId(
        @Param("mentoringId") Long mentoringId,
        Pageable pageable
    );

    @Query("""
        SELECT r.id
        FROM Review r
        WHERE r.reservation.id = :reservationId
        """)
    Optional<Long> findReviewIdByReservationId(
        @Param("reservationId") Long reservationId
    );
}
