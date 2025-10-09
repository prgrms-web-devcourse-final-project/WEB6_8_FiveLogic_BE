package com.back.domain.member.member.verification;

import com.back.domain.member.member.email.EmailService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final VerificationCodeStore codeStore;
    private final EmailService emailService;
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final SecureRandom random = new SecureRandom();

    public String generateAndSendCode(String email) {
        // 6자리 랜덤 코드 생성
        String code = String.format("%06d", random.nextInt(1000000));

        // 저장
        codeStore.saveCode(email, code, CODE_TTL);

        // 이메일 발송
        emailService.sendVerificationCode(email, code);
        log.info("Generated and sent verification code for {}: {}", email, code);

        return code; // 테스트용으로 반환 (실제로는 이메일로만 전송)
    }

    public void verifyCode(String email, String inputCode) {
        Optional<String> storedCode = codeStore.getCode(email);

        if (storedCode.isEmpty()) {
            throw new ServiceException("400-1", "인증번호가 존재하지 않거나 만료되었습니다.");
        }

        if (!storedCode.get().equals(inputCode)) {
            throw new ServiceException("400-2", "인증번호가 일치하지 않습니다.");
        }

        // 인증 성공 시 코드 삭제
        codeStore.deleteCode(email);
        log.info("Email verification successful for: {}", email);
    }
}