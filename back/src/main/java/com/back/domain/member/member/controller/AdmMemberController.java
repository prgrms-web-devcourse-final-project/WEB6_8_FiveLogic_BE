package com.back.domain.member.member.controller;


import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AdmMemberController {
    private final MemberService memberService;
    private final Rq rq;



}
