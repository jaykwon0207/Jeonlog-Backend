package com.jeonlog.exhibition_recommender.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPushToken is a Querydsl query type for PushToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPushToken extends EntityPathBase<PushToken> {

    private static final long serialVersionUID = 1476319668L;

    public static final QPushToken pushToken = new QPushToken("pushToken");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath platform = createString("platform");

    public final StringPath token = createString("token");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPushToken(String variable) {
        super(PushToken.class, forVariable(variable));
    }

    public QPushToken(Path<? extends PushToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPushToken(PathMetadata metadata) {
        super(PushToken.class, metadata);
    }

}

