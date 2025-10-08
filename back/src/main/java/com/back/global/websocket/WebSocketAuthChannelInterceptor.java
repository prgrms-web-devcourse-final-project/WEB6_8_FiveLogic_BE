package com.back.global.websocket;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final MemberService memberService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("STOMP CONNECT attempt.");

            String token = Optional.ofNullable(accessor.getSessionAttributes())
                    .map(attrs -> (String) attrs.get("accessToken"))
                    .orElse(null);

            if (token == null || token.isBlank()) {
                log.error("WebSocket connection rejected: No accessToken found in session attributes.");
                throw new IllegalArgumentException("Missing access token for WebSocket connection.");
            }

            try {
                Map<String, Object> payload = memberService.payload(token);
                String email = (String) payload.get("email");

                Optional<Member> memberOpt = memberService.findByEmail(email);

                if (memberOpt.isEmpty()) {
                    log.error("WebSocket connection rejected: User not found for email {}", email);
                    throw new IllegalArgumentException("Invalid user.");
                }

                Member member = memberOpt.get();

                SecurityUser user = new SecurityUser(
                        member.getId(),
                        member.getEmail(),
                        "", // Password is not needed here
                        member.getName(),
                        member.getNickname(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
                );

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                );

                accessor.setUser(authentication);
                log.info("STOMP user authenticated successfully: {}", user.getUsername());

            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Authentication failed.", e);
            }
        }

        return message;
    }
}
