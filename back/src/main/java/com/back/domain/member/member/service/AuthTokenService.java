package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
public class AuthTokenService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    @Value("${custom.refreshToken.expirationSeconds}")
    private int refreshTokenExpirationSeconds;

    public String genAccessToken(Member member) {
        String email = member.getEmail();
        String name = member.getName();
        String role = member.getRole().name();

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSeconds,
                Map.of("email", email, "name", name, "role", role)
        );
    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);
        if (parsedPayload == null) return null;

        String email = (String) parsedPayload.get("email");
        String name = (String) parsedPayload.get("name");
        String role = (String) parsedPayload.get("role");

        return Map.of("email", email, "name", name, "role", role);
    }

    public String genRefreshToken(Member member) {
        String email = member.getEmail();
        String name = member.getName();
        String role = member.getRole().name();

        return Ut.jwt.toString(
                jwtSecretKey,
                refreshTokenExpirationSeconds,
                Map.of("email", email, "name", name, "role", role, "type", "refresh")
        );
    }

    public boolean isRefreshToken(String token) {
        Map<String, Object> payload = Ut.jwt.payload(jwtSecretKey, token);
        if (payload == null) return false;

        return "refresh".equals(payload.get("type"));
    }

    public boolean isValidToken(String token) {
        return Ut.jwt.payload(jwtSecretKey, token) != null;
    }
}