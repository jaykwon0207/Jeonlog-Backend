package com.jeonlog.exhibition_recommender.exhibition.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVenue is a Querydsl query type for Venue
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVenue extends EntityPathBase<Venue> {

    private static final long serialVersionUID = 1676671040L;

    public static final QVenue venue = new QVenue("venue");

    public final StringPath address = createString("address");

    public final StringPath backgroundImageUrl = createString("backgroundImageUrl");

    public final StringPath description = createString("description");

    public final StringPath email = createString("email");

    public final ListPath<Exhibition, QExhibition> exhibitions = this.<Exhibition, QExhibition>createList("exhibitions", Exhibition.class, QExhibition.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final StringPath logoImageUrl = createString("logoImageUrl");

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final StringPath openingHours = createString("openingHours");

    public final StringPath parkingFee = createString("parkingFee");

    public final StringPath phone = createString("phone");

    public final ListPath<VenuePhoto, QVenuePhoto> photos = this.<VenuePhoto, QVenuePhoto>createList("photos", VenuePhoto.class, QVenuePhoto.class, PathInits.DIRECT2);

    public final EnumPath<VenueType> type = createEnum("type", VenueType.class);

    public final StringPath website = createString("website");

    public QVenue(String variable) {
        super(Venue.class, forVariable(variable));
    }

    public QVenue(Path<? extends Venue> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVenue(PathMetadata metadata) {
        super(Venue.class, metadata);
    }

}

