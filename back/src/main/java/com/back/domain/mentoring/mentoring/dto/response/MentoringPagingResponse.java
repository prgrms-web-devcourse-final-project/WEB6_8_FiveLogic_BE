package com.back.domain.mentoring.mentoring.dto.response;

import com.back.domain.mentoring.mentoring.dto.MentoringDto;
import org.springframework.data.domain.Page;

import java.util.List;

public record MentoringPagingResponse(
    List<MentoringDto> mentorings,
    int currentPage,
    int totalPage,
    long totalElements,
    boolean hasNext
) {
    public static MentoringPagingResponse from(Page<MentoringDto> page) {
        return new MentoringPagingResponse(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext()
        );
    }
}
