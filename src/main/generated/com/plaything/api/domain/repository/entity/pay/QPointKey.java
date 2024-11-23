package com.plaything.api.domain.repository.entity.pay;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPointKey is a Querydsl query type for PointKey
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointKey extends EntityPathBase<PointKey> {

    private static final long serialVersionUID = -1690843183L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPointKey pointKey = new QPointKey("pointKey");

    public final com.plaything.api.domain.repository.entity.common.QBaseEntity _super = new com.plaything.api.domain.repository.entity.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> expirationDate = createDateTime("expirationDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isValidKey = createBoolean("isValidKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final EnumPath<com.plaything.api.domain.key.constant.PointStatus> status = createEnum("status", com.plaything.api.domain.key.constant.PointStatus.class);

    public final StringPath transactionId = createString("transactionId");

    public final com.plaything.api.domain.repository.entity.user.QUser user;

    public QPointKey(String variable) {
        this(PointKey.class, forVariable(variable), INITS);
    }

    public QPointKey(Path<? extends PointKey> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPointKey(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPointKey(PathMetadata metadata, PathInits inits) {
        this(PointKey.class, metadata, inits);
    }

    public QPointKey(Class<? extends PointKey> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.plaything.api.domain.repository.entity.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

