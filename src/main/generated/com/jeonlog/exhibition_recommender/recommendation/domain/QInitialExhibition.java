package com.jeonlog.exhibition_recommender.recommendation.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInitialExhibition is a Querydsl query type for InitialExhibition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInitialExhibition extends EntityPathBase<InitialExhibition> {

    private static final long serialVersionUID = -34798630L;

    public static final QInitialExhibition initialExhibition = new QInitialExhibition("initialExhibition");

    public final EnumPath<com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme> exhibitionTheme = createEnum("exhibitionTheme", com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme.class);

    public final EnumPath<com.jeonlog.exhibition_recommender.exhibition.domain.GenreType> genre = createEnum("genre", com.jeonlog.exhibition_recommender.exhibition.domain.GenreType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath posterUrl = createString("posterUrl");

    public QInitialExhibition(String variable) {
        super(InitialExhibition.class, forVariable(variable));
    }

    public QInitialExhibition(Path<? extends InitialExhibition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInitialExhibition(PathMetadata metadata) {
        super(InitialExhibition.class, metadata);
    }

}

