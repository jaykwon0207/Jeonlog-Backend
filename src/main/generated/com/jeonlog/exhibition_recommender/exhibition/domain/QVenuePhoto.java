package com.jeonlog.exhibition_recommender.exhibition.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVenuePhoto is a Querydsl query type for VenuePhoto
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVenuePhoto extends EntityPathBase<VenuePhoto> {

    private static final long serialVersionUID = 1561951698L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVenuePhoto venuePhoto = new QVenuePhoto("venuePhoto");

    public final StringPath caption = createString("caption");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isCover = createBoolean("isCover");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final QVenue venue;

    public QVenuePhoto(String variable) {
        this(VenuePhoto.class, forVariable(variable), INITS);
    }

    public QVenuePhoto(Path<? extends VenuePhoto> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVenuePhoto(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVenuePhoto(PathMetadata metadata, PathInits inits) {
        this(VenuePhoto.class, metadata, inits);
    }

    public QVenuePhoto(Class<? extends VenuePhoto> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.venue = inits.isInitialized("venue") ? new QVenue(forProperty("venue")) : null;
    }

}

