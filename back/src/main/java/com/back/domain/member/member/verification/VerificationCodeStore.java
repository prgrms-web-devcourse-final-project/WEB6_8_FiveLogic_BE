package com.back.domain.member.member.verification;

import java.time.Duration;
import java.util.Optional;

public interface VerificationCodeStore {
    void saveCode(String email, String code, Duration ttl);
    Optional<String> getCode(String email);
    void deleteCode(String email);
}