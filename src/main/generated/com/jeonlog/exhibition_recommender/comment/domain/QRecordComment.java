package com.jeonlog.exhibition_recommender.comment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecordComment is a Querydsl query type for RecordComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecordComment extends EntityPathBase<RecordComment> {

    private static final long serialVersionUID = -1675871017L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecordComment recordComment = new QRecordComment("recordComment");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QRecordComment parent;

    public final com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord record;

    public final ListPath<RecordComment, QRecordComment> replies = this.<RecordComment, QRecordComment>createList("replies", RecordComment.class, QRecordComment.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.jeonlog.exhibition_recommender.user.domain.QUser user;

    public QRecordComment(String variable) {
        this(RecordComment.class, forVariable(variable), INITS);
    }

    public QRecordComment(Path<? extends RecordComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecordComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecordComment(PathMetadata metadata, PathInits inits) {
        this(RecordComment.class, metadata, inits);
    }

    public QRecordComment(Class<? extends RecordComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QRecordComment(forProperty("parent"), inits.get("parent")) : null;
        this.record = inits.isInitialized("record") ? new com.jeonlog.exhibition_recommender.record.domain.QExhibitionRecord(forProperty("record"), inits.get("record")) : null;
        this.user = inits.isInitialized("user") ? new com.jeonlog.exhibition_recommender.user.domain.QUser(forProperty("user")) : null;
    }

}

