package com.jeonlog.exhibition_recommender.exhibition.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExhibition is a Querydsl query type for Exhibition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExhibition extends EntityPathBase<Exhibition> {

    private static final long serialVersionUID = 931299998L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExhibition exhibition = new QExhibition("exhibition");

    public final ListPath<Artist, QArtist> artists = this.<Artist, QArtist>createList("artists", Artist.class, QArtist.class, PathInits.DIRECT2);

    public final StringPath contact = createString("contact");

    public final StringPath description = createString("description");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final ListPath<ExhibitionGenre, QExhibitionGenre> exhibitionGenres = this.<ExhibitionGenre, QExhibitionGenre>createList("exhibitionGenres", ExhibitionGenre.class, QExhibitionGenre.class, PathInits.DIRECT2);

    public final EnumPath<ExhibitionTheme> exhibitionTheme = createEnum("exhibitionTheme", ExhibitionTheme.class);

    public final StringPath generalRecommendationsPosterUrl = createString("generalRecommendationsPosterUrl");

    public final EnumPath<GenreType> genre = createEnum("genre", GenreType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isFree = createBoolean("isFree");

    public final StringPath location = createString("location");

    public final StringPath personalizedPosterUrl = createString("personalizedPosterUrl");

    public final StringPath posterUrl = createString("posterUrl");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

    public final QVenue venue;

    public final StringPath viewingTime = createString("viewingTime");

    public final StringPath website = createString("website");

    public QExhibition(String variable) {
        this(Exhibition.class, forVariable(variable), INITS);
    }

    public QExhibition(Path<? extends Exhibition> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExhibition(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExhibition(PathMetadata metadata, PathInits inits) {
        this(Exhibition.class, metadata, inits);
    }

    public QExhibition(Class<? extends Exhibition> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.venue = inits.isInitialized("venue") ? new QVenue(forProperty("venue")) : null;
    }

}

