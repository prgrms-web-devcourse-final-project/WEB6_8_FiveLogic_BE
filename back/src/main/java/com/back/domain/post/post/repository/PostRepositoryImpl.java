package com.back.domain.post.post.repository;

import com.back.domain.member.member.entity.QMember;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.QPost;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPosts(String keyword, Pageable pageable, Post.PostType postType) {
        QPost post = QPost.post;

        BooleanBuilder builder = new BooleanBuilder();

        if(postType != null) {
            builder.and(post.postType.eq(postType));
        }

        if(keyword != null && !keyword.isBlank()) {
            builder.and(
                    post.title.containsIgnoreCase(keyword)
                            .or(post.member.name.containsIgnoreCase(keyword))
            );
        }

        List<Post> content = queryFactory
                .selectFrom(post)
                .where(builder)
                .orderBy(post.createDate.desc())
                .offset(pageable.getOffset()) // 시작점
                .limit(pageable.getPageSize())
                .fetch();

        long total = getTotal(post, builder);

        return new PageImpl<>(content,pageable,total);

    }

    private long getTotal(QPost post, BooleanBuilder builder) {
        Long totalCount = queryFactory
                .select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();

        return totalCount != null ? totalCount : 0L;
    }

}
