package com.back.global.security;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class SecurityUser extends User {
    @Getter
    private Long id;

    @Getter
    private String name;

    @Getter
    private String nickname;

    public SecurityUser(
            Long id,
            String username,
            String password,
            String name,
            String nickname,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password != null ? password : "", authorities);
        this.id = id;
        this.name = name;
        this.nickname = nickname;
    }
}
