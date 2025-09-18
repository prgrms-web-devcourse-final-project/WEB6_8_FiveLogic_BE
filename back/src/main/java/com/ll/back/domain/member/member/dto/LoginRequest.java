package com.ll.back.domain.member.member.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}