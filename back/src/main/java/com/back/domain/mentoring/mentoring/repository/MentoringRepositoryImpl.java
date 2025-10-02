package com.back.domain.mentoring.mentoring.repository;

import com.back.domain.member.member.entity.QMember;
import com.back.domain.member.mentor.entity.QMentor;
import com.back.domain.mentoring.mentoring.entity.Mentoring;
import com.back.domain.mentoring.mentoring.entity.QMentoring;
import com.back.domain.mentoring.mentoring.entity.QMentoringTag;
import com.back.domain.mentoring.mentoring.entity.QTag;
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
        QMentoringTag mentoringTag = QMentoringTag.mentoringTag;
        QTag tag = QTag.tag;

        BooleanBuilder builder = new BooleanBuilder();

        // 제목, 멘토 닉네임, 태그 검색 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                mentoring.title.containsIgnoreCase(keyword)
                    .or(mentor.member.nickname.containsIgnoreCase(keyword))
                    .or(tag.name.containsIgnoreCase(keyword))
            );
        }

        // 조건에 맞는 모든 데이터 조회
        List<Mentoring> content = queryFactory
            .selectFrom(mentoring)
            .distinct()
            .leftJoin(mentoring.mentor, mentor).fetchJoin()
            .leftJoin(mentor.member, member).fetchJoin()
            .leftJoin(mentoring.mentoringTags, mentoringTag)
            .leftJoin(mentoringTag.tag, tag)
            .where(builder)
            .orderBy(mentoring.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = getTotal(mentoring, mentoringTag, tag, builder);

        return new PageImpl<>(content, pageable, total);
    }

    private long getTotal(QMentoring mentoring, QMentoringTag mentoringTag, QTag tag, BooleanBuilder builder) {
        Long totalCount = queryFactory
            .select(mentoring.countDistinct())
            .from(mentoring)
            .leftJoin(mentoring.mentoringTags, mentoringTag)
            .leftJoin(mentoringTag.tag, tag)
            .where(builder)
            .fetchOne();
        return totalCount != null ? totalCount : 0L;
    }
}
