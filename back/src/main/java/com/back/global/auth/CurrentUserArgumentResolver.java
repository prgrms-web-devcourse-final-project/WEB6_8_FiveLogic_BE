package com.back.global.auth;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberRepository memberRepository;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 여기에 구현
        return parameter.hasParameterAnnotation(CurrentUser.class) && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        // 여기에 구현
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", authentication != null ? "존재" : "null");

        if (authentication == null || !authentication.isAuthenticated()) {
            // 인증되지 않은 사용자라면 예외 처리
            log.warn("인증 정보 없음");
            return null;
        }
        Object principal = authentication.getPrincipal();
        log.info("Principal 타입: {}", authentication.getPrincipal().getClass().getSimpleName());
        log.info("Principal 값: {}", principal);

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long memberId = securityUser.getId();
        log.info("Member ID: {}", memberId);



        Member member = memberRepository.findById(memberId).orElse(null);
        log.info("Member 조회 결과: {}", member != null ? "성공" : "실패");


        return member;
    }
}
