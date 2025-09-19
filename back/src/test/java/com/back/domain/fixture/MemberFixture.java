package com.back.domain.fixture;

import com.back.domain.member.member.entity.Member;

public class MemberFixture {
    private String email = "test@example.com";
    private String password = "password123";
    private String name = "Test User";
    private Member.Role role = Member.Role.MENTEE;
    private Integer id = null;

    private static MemberFixture builder() {
        return new MemberFixture();
    }

    public static Member createDefault() {
        return builder().build();
    }

    public MemberFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public MemberFixture withPassword(String password) {
        this.password = password;
        return this;
    }

    public MemberFixture withName(String name) {
        this.name = name;
        return this;
    }

    public MemberFixture withRole(Member.Role role) {
        this.role = role;
        return this;
    }

    public MemberFixture withId(Integer id) {
        this.id = id;
        return this;
    }

    public Member build() {
        if (id != null) {
            return new Member(id, email, name);
        }
        return new Member(email, password, name, role);
    }
}
