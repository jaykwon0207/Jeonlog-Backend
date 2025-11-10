package com.jeonlog.exhibition_recommender.exhibition.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExhibitionClickLog is a Querydsl query type for ExhibitionClickLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExhibitionClickLog extends EntityPathBase<ExhibitionClickLog> {

    private static final long serialVersionUID = 301891930L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExhibitionClickLog exhibitionClickLog = new QExhibitionClickLog("exhibitionClickLog");

    public final DatePath<java.time.LocalDate> clickedDate = createDate("clickedDate", java.time.LocalDate.class);

    public final QExhibition exhibition;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.jeonlog.exhibition_recommender.user.domain.QUser user;

    public QExhibitionClickLog(String variable) {
        this(ExhibitionClickLog.class, forVariable(variable), INITS);
    }

    public QExhibitionClickLog(Path<? extends ExhibitionClickLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExhibitionClickLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExhibitionClickLog(PathMetadata metadata, PathInits inits) {
        this(ExhibitionClickLog.class, metadata, inits);
    }

    public QExhibitionClickLog(Class<? extends ExhibitionClickLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.exhibition = inits.isInitialized("exhibition") ? new QExhibition(forProperty("exhibition"), inits.get("exhibition")) : null;
        this.user = inits.isInitialized("user") ? new com.jeonlog.exhibition_recommender.user.domain.QUser(forProperty("user")) : null;
    }

}

