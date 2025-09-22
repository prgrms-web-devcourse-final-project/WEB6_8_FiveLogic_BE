package com.back.domain.post.rq;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private String message;
    private T data;


    public ApiResponse(String message, T data) {
        this.message= message;
        this.data = data;
    }
}
