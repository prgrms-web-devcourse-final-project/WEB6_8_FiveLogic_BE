package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.member.member.entity.QMember;
import com.back.domain.member.mentor.entity.QMentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.QMentoring;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class MentoringRepositoryImpl implements MentoringRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Mentoring> searchMentorings(String keyword, Pageable pageable) {
        QMentoring mentoring = QMentoring.mentoring;
        QMentor mentor = QMentor.mentor;
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        // 제목, 멘토 이름 검색 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                mentoring.title.containsIgnoreCase(keyword)
                    .or(mentor.member.name.containsIgnoreCase(keyword))
            );
        }

        // 1. 조건에 맞는 모든 데이터 조회 (태그 제외)
        List<Mentoring> content = queryFactory
            .selectFrom(mentoring)
            .leftJoin(mentoring.mentor, mentor).fetchJoin()
            .leftJoin(mentor.member, member).fetchJoin()
            .where(builder)
            .orderBy(mentoring.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 2. 태그 검색
        // TODO: 태그 테이블 분리 후 추가 예정

        long total = getTotal(mentoring, builder);

        return new PageImpl<>(content, pageable, total);
    }

    private long getTotal(QMentoring mentoring, BooleanBuilder builder) {
        Long totalCount = queryFactory
            .select(mentoring.count())
            .from(mentoring)
            .where(builder)
            .fetchOne();
        return totalCount != null ? totalCount : 0L;
    }
}
