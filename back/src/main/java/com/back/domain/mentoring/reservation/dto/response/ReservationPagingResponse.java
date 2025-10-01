package com.back.domain.mentoring.reservation.dto.response;

import com.back.domain.mentoring.reservation.dto.ReservationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record ReservationPagingResponse(
    @Schema(description = "예약 목록")
    List<ReservationDto> reservations,
    @Schema(description = "현재 페이지 (0부터 시작)")
    int currentPage,
    @Schema(description = "총 페이지")
    int totalPage,
    @Schema(description = "총 개수")
    long totalElements,
    @Schema(description = "다음 페이지 존재 여부")
    boolean hasNext
) {
    public static ReservationPagingResponse from(Page<ReservationDto> page) {
        return new ReservationPagingResponse(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext()
        );
    }
}
