package com.jeonlog.exhibition_recommender.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QServiceAnnouncement is a Querydsl query type for ServiceAnnouncement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QServiceAnnouncement extends EntityPathBase<ServiceAnnouncement> {

    private static final long serialVersionUID = -447528687L;

    public static final QServiceAnnouncement serviceAnnouncement = new QServiceAnnouncement("serviceAnnouncement");

    public final StringPath body = createString("body");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath pushEnabled = createBoolean("pushEnabled");

    public final StringPath title = createString("title");

    public QServiceAnnouncement(String variable) {
        super(ServiceAnnouncement.class, forVariable(variable));
    }

    public QServiceAnnouncement(Path<? extends ServiceAnnouncement> path) {
        super(path.getType(), path.getMetadata());
    }

    public QServiceAnnouncement(PathMetadata metadata) {
        super(ServiceAnnouncement.class, metadata);
    }

}

