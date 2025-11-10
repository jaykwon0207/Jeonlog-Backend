package com.jeonlog.exhibition_recommender.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserVisit is a Querydsl query type for UserVisit
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserVisit extends EntityPathBase<UserVisit> {

    private static final long serialVersionUID = 985123861L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserVisit userVisit = new QUserVisit("userVisit");

    public final com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition exhibition;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser user;

    public final DatePath<java.time.LocalDate> visitedAt = createDate("visitedAt", java.time.LocalDate.class);

    public QUserVisit(String variable) {
        this(UserVisit.class, forVariable(variable), INITS);
    }

    public QUserVisit(Path<? extends UserVisit> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserVisit(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserVisit(PathMetadata metadata, PathInits inits) {
        this(UserVisit.class, metadata, inits);
    }

    public QUserVisit(Class<? extends UserVisit> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.exhibition = inits.isInitialized("exhibition") ? new com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition(forProperty("exhibition"), inits.get("exhibition")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

