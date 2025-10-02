package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.member.member.entity.QMember;
import com.back.domain.member.mentor.entity.QMentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.QMentoring;
import com.back.domain.mentoring.mentoring.entity.QMentoringTag;
import com.back.domain.mentoring.mentoring.entity.QTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
        QMentoringTag mentoringTag = QMentoringTag.mentoringTag;
        QTag tag = QTag.tag;

        BooleanBuilder builder = new BooleanBuilder();

        // 제목, 멘토 닉네임, 태그 검색 조건
        if (keyword != null && !keyword.isBlank()) {
            // 제목, 멘토 닉네임 검색 조건
            BooleanExpression titleOrNickName = mentoring.title.containsIgnoreCase(keyword)
                .or(mentor.member.nickname.containsIgnoreCase(keyword));

            // 태그 검색 조건 (EXISTS 서브쿼리)
            BooleanExpression tagSearch = JPAExpressions
                .selectOne()
                .from(mentoringTag)
                .join(mentoringTag.tag, tag)
                .where(
                    mentoringTag.mentoring.eq(mentoring)
                    .and(tag.name.containsIgnoreCase(keyword))
                )
                .exists();

            builder.and(titleOrNickName).or(tagSearch);
        }

        // 조건에 맞는 모든 데이터 조회
        List<Mentoring> content = queryFactory
            .selectFrom(mentoring)
            .leftJoin(mentoring.mentor, mentor).fetchJoin()
            .leftJoin(mentor.member, member).fetchJoin()
            .where(builder)
            .orderBy(mentoring.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = getTotal(mentoring, mentor, builder);

        return new PageImpl<>(content, pageable, total);
    }

    private long getTotal(QMentoring mentoring, QMentor mentor, BooleanBuilder builder) {
        Long totalCount = queryFactory
            .select(mentoring.count())
            .from(mentoring)
            .leftJoin(mentoring.mentor, mentor)
            .where(builder)
            .fetchOne();
        return totalCount != null ? totalCount : 0L;
    }
}
