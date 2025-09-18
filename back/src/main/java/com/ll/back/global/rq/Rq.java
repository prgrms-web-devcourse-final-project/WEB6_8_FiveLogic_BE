package com.ll.back.global.rq;


import com.ll.back.domain.member.member.entity.Member;
import com.ll.back.domain.member.member.service.MemberService;
import com.ll.back.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    @Value("${custom.site.cookieDomain}")
    private String cookieDomain;

    @Value("${custom.cookie.httpOnly}")
    private boolean cookieHttpOnly;

    @Value("${custom.cookie.secure}")
    private boolean cookieSecure;

    @Value("${custom.cookie.sameSite}")
    private String cookieSameSite;

    @Value("${custom.cookie.maxAge}")
    private int cookieMaxAge;

    public Member getActor() {
        return Optional.ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                )
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof SecurityUser)
                .map(principal -> (SecurityUser) principal)
                .map(securityUser -> new Member(
                        securityUser.getId(),
                        securityUser.getUsername(),  // email
                        securityUser.getName()
                ))
                .orElse(null);
    }

    public Optional<Member> getActorFromDb() {
        Member actor = getActor();
        if (actor == null) {
            return Optional.empty();
        }
        return memberService.findByEmail(actor.getEmail());
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(cookies ->
                        Arrays.stream(cookies)
                                .filter(cookie -> cookie.getName().equals(name))
                                .map(Cookie::getValue)
                                .filter(value -> !value.isBlank())
                                .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setDomain(cookieDomain);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", cookieSameSite);

        if (value.isBlank()) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(cookieMaxAge);
        }

        resp.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, "");
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public void setHeader(String name, String value) {
        if (value == null) value = "";
        if (value.isBlank()) {
            // 빈 값이면 헤더 제거 (불가능하므로 빈 값 설정)
            resp.setHeader(name, "");
        } else {
            resp.setHeader(name, value);
        }
    }
}