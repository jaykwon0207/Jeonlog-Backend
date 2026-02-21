package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.Set;

import static com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord.exhibitionRecord;
import static com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition.exhibition;
import static com.jeonlog.exhibition_recommender.record.domain.QHashtag.hashtag;

@Repository
public class ExhibitionRecordRepositoryCustomImpl implements ExhibitionRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ExhibitionRecordRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ExhibitionRecord> search(String query, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(query)) {
            // 전시 제목(title)에 query가 포함되거나
            builder.or(exhibition.title.containsIgnoreCase(query));

            // 해시태그 이름(name)에 query가 포함되거나
            builder.or(hashtag.name.containsIgnoreCase(query));

            builder.or(exhibitionRecord.content.contains(query));
        }

        // 데이터 조회
        JPAQuery<ExhibitionRecord> contentQuery = queryFactory
                .selectFrom(exhibitionRecord)
                .join(exhibitionRecord.exhibition, exhibition)
                .leftJoin(exhibitionRecord.hashtags, hashtag)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct();

        List<ExhibitionRecord> content = contentQuery.fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(exhibitionRecord.countDistinct())
                .from(exhibitionRecord)
                .join(exhibitionRecord.exhibition, exhibition)
                .leftJoin(exhibitionRecord.hashtags, hashtag)
                .where(builder);

        // Page 객체로 변환하여 반환
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ExhibitionRecord> findAllExcludingUsers(Set<Long> excludedUserIds, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(excludedUserFilter(excludedUserIds));

        JPAQuery<ExhibitionRecord> contentQuery = queryFactory
                .selectFrom(exhibitionRecord)
                .where(where)
                .orderBy(exhibitionRecord.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<ExhibitionRecord> content = contentQuery.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(exhibitionRecord.count())
                .from(exhibitionRecord)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ExhibitionRecord> searchExcludingUsers(String query, Set<Long> excludedUserIds, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        if (StringUtils.hasText(query)) {
            where.or(exhibition.title.containsIgnoreCase(query));
            where.or(hashtag.name.containsIgnoreCase(query));
            where.or(exhibitionRecord.content.contains(query));
        }
        where.and(excludedUserFilter(excludedUserIds));

        JPAQuery<ExhibitionRecord> contentQuery = queryFactory
                .selectFrom(exhibitionRecord)
                .join(exhibitionRecord.exhibition, exhibition)
                .leftJoin(exhibitionRecord.hashtags, hashtag)
                .where(where)
                .orderBy(exhibitionRecord.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct();

        List<ExhibitionRecord> content = contentQuery.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(exhibitionRecord.countDistinct())
                .from(exhibitionRecord)
                .join(exhibitionRecord.exhibition, exhibition)
                .leftJoin(exhibitionRecord.hashtags, hashtag)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ExhibitionRecord> findByUserExcludingUsers(User target, Set<Long> excludedUserIds, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(exhibitionRecord.user.eq(target));
        where.and(excludedUserFilter(excludedUserIds));

        JPAQuery<ExhibitionRecord> contentQuery = queryFactory
                .selectFrom(exhibitionRecord)
                .where(where)
                .orderBy(exhibitionRecord.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<ExhibitionRecord> content = contentQuery.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(exhibitionRecord.count())
                .from(exhibitionRecord)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder excludedUserFilter(Set<Long> excludedUserIds) {
        BooleanBuilder filter = new BooleanBuilder();
        if (excludedUserIds != null && !excludedUserIds.isEmpty()) {
            filter.and(exhibitionRecord.user.id.notIn(excludedUserIds));
        }
        return filter;
    }
}
