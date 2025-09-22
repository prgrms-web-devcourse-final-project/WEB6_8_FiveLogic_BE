package com.back.domain.Post.rq;

public class ApiResponse<T> {
    private String message;
    private T data;


    public ApiResponse(String message, T data) {
        this.message= message;
        this.data = data;
    }
}
