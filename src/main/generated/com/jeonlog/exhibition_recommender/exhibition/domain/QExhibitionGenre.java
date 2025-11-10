package com.jeonlog.exhibition_recommender.exhibition.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExhibitionGenre is a Querydsl query type for ExhibitionGenre
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExhibitionGenre extends EntityPathBase<ExhibitionGenre> {

    private static final long serialVersionUID = 292850053L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExhibitionGenre exhibitionGenre = new QExhibitionGenre("exhibitionGenre");

    public final QExhibition exhibition;

    public final QGenre genre;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QExhibitionGenre(String variable) {
        this(ExhibitionGenre.class, forVariable(variable), INITS);
    }

    public QExhibitionGenre(Path<? extends ExhibitionGenre> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExhibitionGenre(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExhibitionGenre(PathMetadata metadata, PathInits inits) {
        this(ExhibitionGenre.class, metadata, inits);
    }

    public QExhibitionGenre(Class<? extends ExhibitionGenre> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.exhibition = inits.isInitialized("exhibition") ? new QExhibition(forProperty("exhibition"), inits.get("exhibition")) : null;
        this.genre = inits.isInitialized("genre") ? new QGenre(forProperty("genre")) : null;
    }

}

