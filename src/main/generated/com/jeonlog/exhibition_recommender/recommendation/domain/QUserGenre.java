package com.jeonlog.exhibition_recommender.recommendation.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserGenre is a Querydsl query type for UserGenre
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserGenre extends EntityPathBase<UserGenre> {

    private static final long serialVersionUID = -1128174817L;

    public static final QUserGenre userGenre = new QUserGenre("userGenre");

    public final MapPath<com.jeonlog.exhibition_recommender.exhibition.domain.GenreType, Double, NumberPath<Double>> genreWeights = this.<com.jeonlog.exhibition_recommender.exhibition.domain.GenreType, Double, NumberPath<Double>>createMap("genreWeights", com.jeonlog.exhibition_recommender.exhibition.domain.GenreType.class, Double.class, NumberPath.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final MapPath<com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme, Double, NumberPath<Double>> themeWeights = this.<com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme, Double, NumberPath<Double>>createMap("themeWeights", com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme.class, Double.class, NumberPath.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserGenre(String variable) {
        super(UserGenre.class, forVariable(variable));
    }

    public QUserGenre(Path<? extends UserGenre> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserGenre(PathMetadata metadata) {
        super(UserGenre.class, metadata);
    }

}

