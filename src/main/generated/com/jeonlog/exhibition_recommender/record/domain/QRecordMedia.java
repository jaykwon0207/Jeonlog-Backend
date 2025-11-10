package com.jeonlog.exhibition_recommender.record.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecordMedia is a Querydsl query type for RecordMedia
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecordMedia extends EntityPathBase<RecordMedia> {

    private static final long serialVersionUID = -438771870L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecordMedia recordMedia = new QRecordMedia("recordMedia");

    public final NumberPath<Integer> durationSeconds = createNumber("durationSeconds", Integer.class);

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<MediaType> mediaType = createEnum("mediaType", MediaType.class);

    public final QExhibitionRecord record;

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public QRecordMedia(String variable) {
        this(RecordMedia.class, forVariable(variable), INITS);
    }

    public QRecordMedia(Path<? extends RecordMedia> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecordMedia(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecordMedia(PathMetadata metadata, PathInits inits) {
        this(RecordMedia.class, metadata, inits);
    }

    public QRecordMedia(Class<? extends RecordMedia> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.record = inits.isInitialized("record") ? new QExhibitionRecord(forProperty("record"), inits.get("record")) : null;
    }

}

