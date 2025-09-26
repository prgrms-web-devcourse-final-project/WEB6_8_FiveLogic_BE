package com.back.domain.mentoring.reservation.controller;

import com.back.domain.member.member.service.MemberStorage;
import com.back.domain.member.mentee.entity.Mentee;
import com.back.domain.mentoring.reservation.dto.request.ReservationRequest;
import com.back.domain.mentoring.reservation.dto.response.ReservationResponse;
import com.back.domain.mentoring.reservation.service.ReservationService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "ReservationController", description = "예약 API")
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberStorage memberStorage;
    private final Rq rq;

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
}
