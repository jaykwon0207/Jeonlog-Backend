package com.jeonlog.exhibition_recommender.like.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecordLike is a Querydsl query type for RecordLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecordLike extends EntityPathBase<RecordLike> {

    private static final long serialVersionUID = -1474777313L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecordLike recordLike = new QRecordLike("recordLike");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> likedAt = createDateTime("likedAt", java.time.LocalDateTime.class);

    public final com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord record;

    public final com.jeonlog.exhibition_recommender.user.domain.QUser user;

    public QRecordLike(String variable) {
        this(RecordLike.class, forVariable(variable), INITS);
    }

    public QRecordLike(Path<? extends RecordLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecordLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecordLike(PathMetadata metadata, PathInits inits) {
        this(RecordLike.class, metadata, inits);
    }

    public QRecordLike(Class<? extends RecordLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.record = inits.isInitialized("record") ? new com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord(forProperty("record"), inits.get("record")) : null;
        this.user = inits.isInitialized("user") ? new com.jeonlog.exhibition_recommender.user.domain.QUser(forProperty("user")) : null;
    }

}

