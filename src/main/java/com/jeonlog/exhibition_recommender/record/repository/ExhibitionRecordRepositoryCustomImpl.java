package com.jeonlog.exhibition_recommender.record.repository;

import com.jeonlog.exhibition_recommender.record.domain.ExhibitionRecord;
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
}