package com.back.domain.mentoring.reservation.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.member.mentor.entity.Mentor;
import com.back.domain.mentoring.reservation.dto.ReservationDto;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationPagingResponse;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.service.ReservationService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "ReservationController", description = "예약 API")
public class ReservationController {

    private final Rq rq;
    private final ReservationService reservationService;
    private final MemberStorage memberStorage;

    @GetMapping
    @Operation(summary = "나의 예약 목록 조회", description = "본인의 예약 목록을 조회합니다. 로그인 후 조회할 수 있습니다.")
    public RsData<ReservationPagingResponse> getReservations(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Member member = rq.getActor();
        Page<ReservationDto> reservationPage = reservationService.getReservations(member, page, size);
        ReservationPagingResponse resDto = ReservationPagingResponse.from(reservationPage);

        return new RsData<>(
            "200",
            "예약 목록을 조회하였습니다.",
            resDto
        );
    }


    @GetMapping("/{reservationId}")
    @Operation(summary = "예약 조회", description = "특정 예약을 조회합니다. 로그인 후 예약 조회할 수 있습니다.")
    public RsData<ReservationResponse> getReservation(
        @PathVariable Long reservationId
    ) {
        Member member = rq.getActor();
        ReservationResponse resDto = reservationService.getReservation(member, reservationId);

        return new RsData<>(
            "200",
            "예약을 조회하였습니다.",
            resDto
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTEE')")
    @Operation(summary = "예약 신청", description = "멘티가 멘토의 슬롯을 선택해 예약 신청을 합니다. 로그인한 멘티만 예약 신청할 수 있습니다.")
    public RsData<ReservationResponse> createReservation(
        @RequestBody @Valid ReservationRequest reqDto
    ) {
        Mentee mentee = memberStorage.findMenteeByMember(rq.getActor());
        ReservationResponse resDto = reservationService.createReservation(mentee, reqDto);

        return new RsData<>(
            "201",
            "예약 신청이 완료되었습니다.",
            resDto
        );
    }

    @PatchMapping("/{reservationId}/approve")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "예약 수락", description = "멘토가 멘티의 예약 신청을 수락합니다. 로그인한 멘토만 예약 수락할 수 있습니다.")
    public RsData<ReservationResponse> approveReservation(
        @PathVariable Long reservationId
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        ReservationResponse resDto = reservationService.approveReservation(mentor, reservationId);

        return new RsData<>(
            "200",
            "예약이 수락되었습니다.",
            resDto
        );
    }

    @PatchMapping("/{reservationId}/reject")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "예약 거절", description = "멘토가 멘티의 예약 신청을 거절합니다. 로그인한 멘토만 예약 거절할 수 있습니다.")
    public RsData<ReservationResponse> rejectReservation(
        @PathVariable Long reservationId
    ) {
        Mentor mentor = memberStorage.findMentorByMember(rq.getActor());
        ReservationResponse resDto = reservationService.rejectReservation(mentor, reservationId);

        return new RsData<>(
            "200",
            "예약이 거절되었습니다.",
            resDto
        );
    }

    @PatchMapping("/{reservationId}/cancel")
    @Operation(summary = "예약 취소", description = "멘토 또는 멘티가 예약을 취소합니다. 로그인 후 예약 취소할 수 있습니다.")
    public RsData<ReservationResponse> cancelReservation(
        @PathVariable Long reservationId
    ) {
        Member member = rq.getActor();
        ReservationResponse resDto = reservationService.cancelReservation(member, reservationId);

        return new RsData<>(
            "200",
            "예약이 취소되었습니다.",
            resDto
        );
    }
}
