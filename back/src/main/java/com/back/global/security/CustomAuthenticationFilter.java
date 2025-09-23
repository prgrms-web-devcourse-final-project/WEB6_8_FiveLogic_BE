package com.back.global.security;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final MemberService memberService;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("Processing request for " + request.getRequestURI());

        try {
            work(request, response, filterChain);
        } catch (Exception e) {
            RsData<Void> rsData = new RsData<>("401-1", "인증 오류가 발생했습니다.");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(
                    Ut.json.toString(rsData)
            );
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 인증이 필요없는 API 요청이라면 패스
        if (List.of("/auth/login", "/auth/refresh", "/h2-console").contains(request.getRequestURI()) ||
                request.getRequestURI().startsWith("/auth/signup") ||
                request.getRequestURI().startsWith("/h2-console/") ||
                request.getRequestURI().startsWith("/swagger-ui/") ||
                request.getRequestURI().startsWith("/v3/api-docs/") ||
                request.getRequestURI().startsWith("/swagger-resources/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = rq.getCookieValue("accessToken", "");

        if (accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // MemberService의 payload 메서드 사용
        Map<String, Object> payload = memberService.payload(accessToken);

        if (payload == null) {
            String refreshToken = rq.getCookieValue("refreshToken", "");
            if (!refreshToken.isBlank() && memberService.isValidToken(refreshToken) && memberService.isRefreshToken(refreshToken)) {
                // Refresh token에서 사용자 정보 추출
                Map<String, Object> refreshPayload = memberService.payload(refreshToken);
                if (refreshPayload != null) {
                    String email = (String) refreshPayload.get("email");
                    Optional<Member> memberOpt = memberService.findByEmail(email);
                    if (memberOpt.isPresent()) {
                        Member member = memberOpt.get();
                        // 새로운 access token 생성 및 설정
                        String newAccessToken = memberService.genAccessToken(member);
                        rq.setCookie("accessToken", newAccessToken);

                        // 새 토큰으로 다시 payload 추출
                        payload = memberService.payload(newAccessToken);
                    }
                }
            }

            if (payload == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String email = (String) payload.get("email");

        Optional<Member> memberOpt = memberService.findByEmail(email);
        if (memberOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Member member = memberOpt.get();

        // SecurityUser 생성 및 인증 설정
        SecurityUser user = new SecurityUser(
                member.getId(),
                member.getEmail(),
                "",
                member.getName(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
