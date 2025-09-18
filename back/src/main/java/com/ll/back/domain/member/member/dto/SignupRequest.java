package com.ll.back.domain.member.member.dto;

import com.ll.back.domain.member.member.entity.Member;
import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private Member.Role role;
}