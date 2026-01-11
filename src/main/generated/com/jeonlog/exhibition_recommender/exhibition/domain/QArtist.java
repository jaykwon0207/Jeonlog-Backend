package com.jeonlog.exhibition_recommender.exhibition.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArtist is a Querydsl query type for Artist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArtist extends EntityPathBase<Artist> {

    private static final long serialVersionUID = -151843946L;

    public static final QArtist artist = new QArtist("artist");

    public final ListPath<Exhibition, QExhibition> exhibitions = this.<Exhibition, QExhibition>createList("exhibitions", Exhibition.class, QExhibition.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QArtist(String variable) {
        super(Artist.class, forVariable(variable));
    }

    public QArtist(Path<? extends Artist> path) {
        super(path.getType(), path.getMetadata());
    }

    public QArtist(PathMetadata metadata) {
        super(Artist.class, metadata);
    }

}

