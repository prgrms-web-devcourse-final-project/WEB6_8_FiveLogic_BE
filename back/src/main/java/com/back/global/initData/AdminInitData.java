package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class AdminInitData {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initAdmin() {
        return args -> {
            if (memberRepository.findByEmail("admin@admin.com").isEmpty()) {
                Member admin = new Member(
                        "admin@admin.com",
                        passwordEncoder.encode("admin1234"),
                        "관리자",
                        "admin",
                        Member.Role.ADMIN
                );
                memberRepository.save(admin);
                log.info("어드민 계정이 생성되었습니다. (email: admin@admin.com, password: admin1234)");
            } else {
                log.info("어드민 계정이 이미 존재합니다.");
            }
        };
    }

}
