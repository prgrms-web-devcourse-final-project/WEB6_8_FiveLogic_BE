package com.back.domain.post.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCreateRequest {
    private String postType;
    @NotBlank(message = "제목은 null 혹은 공백일 수 없습니다.")
    private String title;
    @NotBlank(message = "제목은 null 혹은 공백일 수 없습니다.")
    private String content;
}
