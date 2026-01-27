package com.jeonlog.exhibition_recommender.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationImage is a Querydsl query type for NotificationImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationImage extends EntityPathBase<NotificationImage> {

    private static final long serialVersionUID = -257563419L;

    public static final QNotificationImage notificationImage = new QNotificationImage("notificationImage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Long> notificationId = createNumber("notificationId", Long.class);

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QNotificationImage(String variable) {
        super(NotificationImage.class, forVariable(variable));
    }

    public QNotificationImage(Path<? extends NotificationImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationImage(PathMetadata metadata) {
        super(NotificationImage.class, metadata);
    }

}

