package com.back.domain.mentoring.mentoring.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.mentoring.dto.request.ReviewRequest;
import com.back.domain.mentoring.mentoring.dto.response.ReviewResponse;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.Review;
import com.back.domain.mentoring.mentoring.error.ReviewErrorCode;
import com.back.domain.mentoring.mentoring.repository.ReviewRepository;
import com.back.domain.mentoring.reservation.constant.ReservationStatus;
import com.back.domain.mentoring.reservation.entity.Reservation;
import com.back.domain.mentoring.slot.entity.MentorSlot;
import com.back.fixture.MemberFixture;
import com.back.fixture.MenteeFixture;
import com.back.fixture.MentorFixture;
import com.back.fixture.mentoring.MentorSlotFixture;
import com.back.fixture.mentoring.MentoringFixture;
import com.back.fixture.mentoring.ReservationFixture;
import com.back.fixture.mentoring.ReviewFixture;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MentoringStorage mentoringStorage;

    private Mentor mentor;
    private Mentee mentee, mentee2;
    private Mentoring mentoring;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Member mentorMember = MemberFixture.create(1L, "mentor@test.com", "Mentor", "pass123", Member.Role.MENTOR);
        mentor = MentorFixture.create(1L, mentorMember);

        Member menteeMember = MemberFixture.create(2L, "mentee@test.com", "Mentee", "pass123", Member.Role.MENTEE);
        mentee = MenteeFixture.create(1L, menteeMember);

        Member menteeMember2 = MemberFixture.create(3L, "mentee2@test.com", "Mentee2", "pass123", Member.Role.MENTEE);
        mentee2 = MenteeFixture.create(2L, menteeMember2);

        mentoring = MentoringFixture.create(1L, mentor);
        MentorSlot mentorSlot = MentorSlotFixture.create(1L, mentor);
        reservation = ReservationFixture.create(1L, mentoring, mentee, mentorSlot);
        ReflectionTestUtils.setField(reservation, "status", ReservationStatus.COMPLETED);
    }

    @Nested
    @DisplayName("멘토링 리뷰 생성")
    class Describe_createReview {

        ReviewRequest request = new ReviewRequest(3.5, "리뷰 내용입니다.");

        @Test
        @DisplayName("리뷰 생성")
        void createReview() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);
            when(reviewRepository.existsByReservationId(reservation.getId()))
                .thenReturn(false);
            when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            when(reviewRepository.findAverageRating(mentor))
                .thenReturn(3.5);

            // when
            ReviewResponse response = reviewService.createReview(reservation.getId(), request, mentee);

            // then
            assertThat(response.rating()).isEqualTo(request.rating());
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.menteeId()).isEqualTo(mentee.getId());

            verify(reviewRepository).save(any(Review.class));
            verify(reviewRepository).findAverageRating(mentor);
            assertThat(mentor.getRate()).isEqualTo(3.5);
        }

        @Test
        @DisplayName("예약 상태가 COMPLETED가 아니면 예외")
        void throwExceptionWhenInvalidReservationStatus() {
            // given
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);

            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(reservation.getId(), request, mentee))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReviewErrorCode.CANNOT_REVIEW.getCode());
        }

        @Test
        @DisplayName("별점이 0.5 단위가 아니면 예외")
        void throwExceptionWhenInvalidRatingUnit() {
            // given
            ReviewRequest request2 = new ReviewRequest(3.2, "리뷰 내용입니다.");

            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(reservation.getId(), request2, mentee))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReviewErrorCode.INVALID_RATING_UNIT.getCode());
        }

        @Test
        @DisplayName("이미 리뷰가 존재하면 예외 발생")
        void throwExceptionWhenAlreadyExistsReview() {
            // given
            when(mentoringStorage.findReservation(reservation.getId()))
                .thenReturn(reservation);
            when(reviewRepository.existsByReservationId(reservation.getId()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(reservation.getId(), request, mentee))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReviewErrorCode.ALREADY_EXISTS_REVIEW.getCode());
        }
    }

    @Nested
    @DisplayName("멘토링 리뷰 수정")
    class Describe_updateReview {

        private Review review;

        @BeforeEach
        void setUp() {
            review = ReviewFixture.create(1L, reservation, mentee);
        }

        @Test
        @DisplayName("리뷰 수정 성공")
        void updateReview() {
            // given
            ReviewRequest updateRequest = new ReviewRequest(4.0, "수정된 리뷰 내용입니다.");

            when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));
            when(reviewRepository.findAverageRating(mentor))
                .thenReturn(4.0);

            // when
            ReviewResponse response = reviewService.updateReview(review.getId(), updateRequest, mentee);

            // then
            assertThat(response.rating()).isEqualTo(updateRequest.rating());
            assertThat(response.content()).isEqualTo(updateRequest.content());
            assertThat(response.menteeId()).isEqualTo(mentee.getId());
            assertThat(mentor.getRate()).isEqualTo(4.0);

            verify(reviewRepository).findById(review.getId());
            verify(reviewRepository).findAverageRating(mentor);
        }

        @Test
        @DisplayName("리뷰 없으면 예외")
        void throwExceptionWhenReviewNotFound() {
            // given
            ReviewRequest updateRequest = new ReviewRequest(4.0, "수정된 리뷰 내용입니다.");

            when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(review.getId(), updateRequest, mentee))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReviewErrorCode.REVIEW_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("리뷰 작성자가 아니면 예외")
        void throwExceptionWhenNotReviewAuthor() {
            // given
            ReviewRequest updateRequest = new ReviewRequest(4.0, "수정된 리뷰 내용입니다.");

            when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(review.getId(), updateRequest, mentee2))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReviewErrorCode.FORBIDDEN_NOT_MENTEE.getCode());
        }
    }

    @Nested
    @DisplayName("멘토링 리뷰 삭제")
    class Describe_deleteReview {

        private Review review;

        @BeforeEach
        void setUp() {
            review = ReviewFixture.create(1L, reservation, mentee);
        }

        @Test
        @DisplayName("리뷰 삭제 성공")
        void deleteReview() {
            // given
            when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));
            // 리뷰 삭제 후 평균 평점이 없을 경우
            when(reviewRepository.findAverageRating(mentor))
                .thenReturn(null);

            // when
            ReviewResponse response = reviewService.deleteReview(review.getId(), mentee);

            // then
            assertThat(response.reviewId()).isEqualTo(review.getId());
            // 평균 평점이 0.0으로 업데이트 되는지 확인
            assertThat(mentor.getRate()).isEqualTo(0.0);

            verify(reviewRepository).findById(review.getId());
            verify(reviewRepository).delete(review);
            verify(reviewRepository).findAverageRating(mentor);
        }

        @Test
        @DisplayName("리뷰 작성자가 아니면 예외")
        void throwExceptionWhenNotReviewAuthor() {
            // given
            when(reviewRepository.findById(review.getId()))
                .thenReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(review.getId(), mentee2))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("resultCode", ReviewErrorCode.FORBIDDEN_NOT_MENTEE.getCode());
        }
    }
}