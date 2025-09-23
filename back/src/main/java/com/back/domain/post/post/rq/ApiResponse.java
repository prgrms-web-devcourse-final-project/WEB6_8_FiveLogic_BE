package com.back.domain.post.post.rq;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private String msg;
    private T data;


    public ApiResponse(String msg, T data) {
        this.msg = msg;
        this.data = data;
    }
}
