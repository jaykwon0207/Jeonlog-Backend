package com.jeonlog.exhibition_recommender.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = 547074582L;

    public static final QNotification notification = new QNotification("notification");

    public final NumberPath<Long> actorUserId = createNumber("actorUserId", Long.class);

    public final StringPath body = createString("body");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath dedupKey = createString("dedupKey");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final DateTimePath<java.time.LocalDateTime> readAt = createDateTime("readAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> receiverUserId = createNumber("receiverUserId", Long.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final EnumPath<TargetType> targetType = createEnum("targetType", TargetType.class);

    public final StringPath title = createString("title");

    public final EnumPath<NotificationType> type = createEnum("type", NotificationType.class);

    public QNotification(String variable) {
        super(Notification.class, forVariable(variable));
    }

    public QNotification(Path<? extends Notification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotification(PathMetadata metadata) {
        super(Notification.class, metadata);
    }

}

