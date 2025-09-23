package com.back.fixture;

import com.back.domain.member.member.entity.Member;

/**
 * Member 엔티티의 테스트용 빌더 클래스입니다.
 * 기본값을 제공하며, 필요에 따라 with-- 체이닝 메소드를 조합하여 Member 객체를 생성하는 메서드를 생성할 수 있도록 했습니다..
 */
public class MemberFixture {
    private String email = "test@example.com";
    private String password = "password123";
    private String name = "Test User";
    private Member.Role role = Member.Role.MENTEE;
    private Long id = null;

    private static MemberFixture builder() {
        return new MemberFixture();
    }

    public static Member createDefault() {
        return builder().build();
    }

    public static Member create(Long id, String email, String name, String password, Member.Role role) {
        return builder()
            .withId(id)
            .withEmail(email)
            .withName(name)
            .withPassword(password)
            .withRole(role)
            .build();
    }

    public static Member create(String email, String name, String password) {
        return builder()
            .withEmail(email)
            .withName(name)
            .withPassword(password)
            .build();
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

    public MemberFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Member build() {
        if (id != null) {
            return new Member(id, email, name, role);
        }
        return new Member(email, password, name, role);
    }
}