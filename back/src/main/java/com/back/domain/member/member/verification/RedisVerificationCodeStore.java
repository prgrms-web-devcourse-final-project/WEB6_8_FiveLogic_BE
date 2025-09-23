package com.back.domain.member.member.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class RedisVerificationCodeStore implements VerificationCodeStore {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveCode(String email, String code, Duration ttl) {
        String key = "verification:" + email;
        redisTemplate.opsForValue().set(key, code, ttl);
    }

    @Override
    public Optional<String> getCode(String email) {
        String key = "verification:" + email;
        String code = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(code);
    }

    @Override
    public void deleteCode(String email) {
        String key = "verification:" + email;
        redisTemplate.delete(key);
    }
}