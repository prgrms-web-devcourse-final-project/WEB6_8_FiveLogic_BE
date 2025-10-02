package com.back.fixture.mentoring;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.mentoring.entity.Review;
import com.back.domain.mentoring.reservation.entity.Reservation;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewFixture {
    
    private static final double DEFAULT_RATING = 4.0;
    private static final String DEFAULT_CONTENT = "멘토링 리뷰입니다.";

    public static Review create(Reservation reservation, Mentee mentee) {
        return Review.builder()
            .reservation(reservation)
            .mentee(mentee)
            .rating(DEFAULT_RATING)
            .content(DEFAULT_CONTENT)
            .build();
    }

    public static Review create(Long id, Reservation reservation, Mentee mentee) {
        Review review = Review.builder()
            .reservation(reservation)
            .mentee(mentee)
            .rating(DEFAULT_RATING)
            .content(DEFAULT_CONTENT)
            .build();
        ReflectionTestUtils.setField(review, "id", id);

        return review;
    }

    public static Review create(Long id, Reservation reservation, Mentee mentee, double rating, String content) {
        Review review = Review.builder()
            .reservation(reservation)
            .mentee(mentee)
            .rating(rating)
            .content(content)
            .build();
        ReflectionTestUtils.setField(review, "id", id);

        return review;
    }
}
