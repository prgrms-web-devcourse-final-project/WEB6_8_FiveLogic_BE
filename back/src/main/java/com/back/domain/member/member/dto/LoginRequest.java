package com.back.domain.member.member.dto;

public record LoginRequest(
    String email,
    String password
) {
}