package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.request.ReviewRequest;
import com.back.domain.mentoring.mentoring.dto.response.ReviewResponse;
import com.back.domain.mentoring.mentoring.entity.Review;
import com.back.domain.mentoring.mentoring.error.ReviewErrorCode;
import com.back.domain.mentoring.mentoring.repository.ReviewRepository;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MentoringStorage mentoringStorage;

    @Transactional
    public ReviewResponse createReview(Long reservationId, ReviewRequest reqDto, Mentee mentee) {
        Reservation reservation = mentoringStorage.findReservation(reservationId);

        validateReservationStatus(reservation);
        validateRating(reqDto.rating());
        validateNoDuplicate(reservation);

        Review review = Review.builder()
            .reservation(reservation)
            .mentee(mentee)
            .rating(reqDto.rating())
            .content(reqDto.content())
            .build();
        reviewRepository.save(review);

        updateMentorRating(reservation.getMentor());

        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest reqDto, Mentee mentee) {
        Review review = findReview(reviewId);

        validateMentee(mentee, review);
        validateRating(reqDto.rating());

        review.update(reqDto.rating(), reqDto.content());
        updateMentorRating(review.getReservation().getMentor());

        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse deleteReview(Long reviewId, Mentee mentee) {
        Review review = findReview(reviewId);

        validateMentee(mentee, review);

        reviewRepository.delete(review);
        updateMentorRating(review.getReservation().getMentor());

        return ReviewResponse.from(review);
    }


    // ===== 평점 업데이트 =====

    private void updateMentorRating(Mentor mentor) {
        Double averageRating = reviewRepository.findAverageRating(mentor);
        mentor.updateRating(averageRating != null ? averageRating : 0.0);
    }


    // ===== 헬퍼 메서드 =====
    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ServiceException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }


    // ===== 유효성 검사 =====

    private static void validateReservationStatus(Reservation reservation) {
        if (!reservation.getStatus().canReview()) {
            throw new ServiceException(ReviewErrorCode.CANNOT_REVIEW);
        }
    }

    private void validateRating(double rating) {
        if ((rating * 10) % 5 != 0) {
            throw new ServiceException(ReviewErrorCode.INVALID_RATING_UNIT);
        }
    }

    private void validateNoDuplicate(Reservation reservation) {
        if (reviewRepository.existsByReservationId(reservation.getId())) {
            throw new ServiceException(ReviewErrorCode.ALREADY_EXISTS_REVIEW);
        }
    }

    private static void validateMentee(Mentee mentee, Review review) {
        if (!review.isMentee(mentee)) {
            throw new ServiceException(ReviewErrorCode.FORBIDDEN_NOT_MENTEE);
        }
    }
}
