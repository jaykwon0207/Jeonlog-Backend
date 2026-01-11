package com.jeonlog.exhibition_recommender.search.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSearch is a Querydsl query type for Search
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearch extends EntityPathBase<Search> {

    private static final long serialVersionUID = -235723760L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSearch search = new QSearch("search");

    public final com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition exhibition;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final DateTimePath<java.time.LocalDateTime> searchedAt = createDateTime("searchedAt", java.time.LocalDateTime.class);

    public final com.jeonlog.exhibition_recommender.user.domain.QUser user;

    public QSearch(String variable) {
        this(Search.class, forVariable(variable), INITS);
    }

    public QSearch(Path<? extends Search> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSearch(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSearch(PathMetadata metadata, PathInits inits) {
        this(Search.class, metadata, inits);
    }

    public QSearch(Class<? extends Search> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.exhibition = inits.isInitialized("exhibition") ? new com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition(forProperty("exhibition"), inits.get("exhibition")) : null;
        this.user = inits.isInitialized("user") ? new com.jeonlog.exhibition_recommender.user.domain.QUser(forProperty("user")) : null;
    }

}

