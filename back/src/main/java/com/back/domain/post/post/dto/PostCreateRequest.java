package com.back.domain.post.post.dto;

import jakarta.validation.constraints.NotBlank;



public record PostCreateRequest(
        String postType,
        @NotBlank(message = "제목은 null 혹은 공백일 수 없습니다.")
        String title,
        @NotBlank(message = "제목은 null 혹은 공백일 수 없습니다.")
        String content
) {


}
