package com.jeonlog.exhibition_recommender.record.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExhibitionRecord is a Querydsl query type for ExhibitionRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExhibitionRecord extends EntityPathBase<ExhibitionRecord> {

    private static final long serialVersionUID = -1691791599L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExhibitionRecord exhibitionRecord = new QExhibitionRecord("exhibitionRecord");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition exhibition;

    public final SetPath<Hashtag, QHashtag> hashtags = this.<Hashtag, QHashtag>createSet("hashtags", Hashtag.class, QHashtag.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final ListPath<RecordMedia, QRecordMedia> mediaList = this.<RecordMedia, QRecordMedia>createList("mediaList", RecordMedia.class, QRecordMedia.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updateAt = createDateTime("updateAt", java.time.LocalDateTime.class);

    public final com.jeonlog.exhibition_recommender.user.domain.QUser user;

    public QExhibitionRecord(String variable) {
        this(ExhibitionRecord.class, forVariable(variable), INITS);
    }

    public QExhibitionRecord(Path<? extends ExhibitionRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExhibitionRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExhibitionRecord(PathMetadata metadata, PathInits inits) {
        this(ExhibitionRecord.class, metadata, inits);
    }

    public QExhibitionRecord(Class<? extends ExhibitionRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.exhibition = inits.isInitialized("exhibition") ? new com.jeonlog.exhibition_recommender.exhibition.domain.QExhibition(forProperty("exhibition"), inits.get("exhibition")) : null;
        this.user = inits.isInitialized("user") ? new com.jeonlog.exhibition_recommender.user.domain.QUser(forProperty("user")) : null;
    }

}

