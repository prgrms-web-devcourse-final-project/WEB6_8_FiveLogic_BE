package com.back.domain.member.member.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"test", "dev", "prod"})
@Slf4j
public class InMemoryVerificationCodeStore implements VerificationCodeStore {
    private final Map<String, CodeData> store = new ConcurrentHashMap<>();

    @Override
    public void saveCode(String email, String code, Duration ttl) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);
        store.put(email, new CodeData(code, expiresAt));
        log.debug("Saved verification code for email: {}", email);
    }

    @Override
    public Optional<String> getCode(String email) {
        CodeData data = store.get(email);
        if (data == null) {
            return Optional.empty();
        }

        if (data.expiresAt.isBefore(LocalDateTime.now())) {
            store.remove(email);
            log.debug("Verification code expired for email: {}", email);
            return Optional.empty();
        }

        return Optional.of(data.code);
    }

    @Override
    public void deleteCode(String email) {
        store.remove(email);
        log.debug("Deleted verification code for email: {}", email);
    }

    @Scheduled(fixedRate = 60000) // 1분마다 만료된 코드 정리
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        store.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
    }

    private record CodeData(String code, LocalDateTime expiresAt) {}
}