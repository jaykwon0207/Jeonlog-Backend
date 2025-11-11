package com.jeonlog.exhibition_recommender.scrap.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecordScrap is a Querydsl query type for RecordScrap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecordScrap extends EntityPathBase<RecordScrap> {

    private static final long serialVersionUID = -819582313L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecordScrap recordScrap = new QRecordScrap("recordScrap");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord record;

    public final DateTimePath<java.time.LocalDateTime> scrappedAt = createDateTime("scrappedAt", java.time.LocalDateTime.class);

    public final com.jeonlog.exhibition_recommender.user.domain.QUser user;

    public QRecordScrap(String variable) {
        this(RecordScrap.class, forVariable(variable), INITS);
    }

    public QRecordScrap(Path<? extends RecordScrap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecordScrap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecordScrap(PathMetadata metadata, PathInits inits) {
        this(RecordScrap.class, metadata, inits);
    }

    public QRecordScrap(Class<? extends RecordScrap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.record = inits.isInitialized("record") ? new com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord(forProperty("record"), inits.get("record")) : null;
        this.user = inits.isInitialized("user") ? new com.jeonlog.exhibition_recommender.user.domain.QUser(forProperty("user")) : null;
    }

}

