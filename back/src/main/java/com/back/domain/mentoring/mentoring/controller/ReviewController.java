package com.back.domain.mentoring.mentoring.controller;

import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.mentoring.dto.request.ReviewRequest;
import com.back.domain.mentoring.mentoring.dto.response.ReviewResponse;
import com.back.domain.mentoring.mentoring.service.ReviewService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "ReviewController", description = "멘토링 리뷰 API")
public class ReviewController {
    private final Rq rq;
    private final MemberStorage memberStorage;
    private final ReviewService reviewService;

    @PostMapping("/reservations/{reservationId}/reviews")
    @PreAuthorize("hasRole('MENTEE')")
    @Operation(summary = "멘토링 리뷰 작성", description = "멘토링 리뷰를 작성합니다.")
    public RsData<ReviewResponse> createReview(
        @PathVariable Long reservationId,
        @RequestBody @Valid ReviewRequest reqDto
    ) {
        Mentee mentee = memberStorage.findMenteeByMember(rq.getActor());
        ReviewResponse resDto = reviewService.createReview(reservationId, reqDto, mentee);

        return new RsData<>(
            "201",
            "멘토링 리뷰가 작성되었습니다.",
            resDto
        );
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('MENTEE')")
    @Operation(summary = "멘토링 리뷰 수정", description = "멘토링 리뷰를 수정합니다.")
    public RsData<ReviewResponse> updateReview(
        @PathVariable Long reviewId,
        @RequestBody @Valid ReviewRequest reqDto
    ) {
        Mentee mentee = memberStorage.findMenteeByMember(rq.getActor());
        ReviewResponse resDto = reviewService.updateReview(reviewId, reqDto, mentee);

        return new RsData<>(
            "200",
            "멘토링 리뷰가 수정되었습니다.",
            resDto
        );
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('MENTEE')")
    @Operation(summary = "멘토링 리뷰 삭제", description = "멘토링 리뷰를 삭제합니다.")
    public RsData<ReviewResponse> deleteReview(
        @PathVariable Long reviewId
    ) {
        Mentee mentee = memberStorage.findMenteeByMember(rq.getActor());
        ReviewResponse resDto = reviewService.deleteReview(reviewId, mentee);

        return new RsData<>(
            "200",
            "멘토링 리뷰가 삭제되었습니다.",
            resDto
        );
    }
}
