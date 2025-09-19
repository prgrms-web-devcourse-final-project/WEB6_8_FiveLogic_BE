package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.mentoring.mentoring.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
