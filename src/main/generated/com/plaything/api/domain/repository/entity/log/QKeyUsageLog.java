package com.plaything.api.domain.repository.entity.log;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QKeyUsageLog is a Querydsl query type for KeyUsageLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKeyUsageLog extends EntityPathBase<KeyUsageLog> {

    private static final long serialVersionUID = -1436086884L;

    public static final QKeyUsageLog keyUsageLog = new QKeyUsageLog("keyUsageLog");

    public final com.plaything.api.domain.repository.entity.common.QBaseLogEntity _super = new com.plaything.api.domain.repository.entity.common.QBaseLogEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> receiverId = createNumber("receiverId", Long.class);

    public final NumberPath<Long> senderId = createNumber("senderId", Long.class);

    public QKeyUsageLog(String variable) {
        super(KeyUsageLog.class, forVariable(variable));
    }

    public QKeyUsageLog(Path<? extends KeyUsageLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QKeyUsageLog(PathMetadata metadata) {
        super(KeyUsageLog.class, metadata);
    }

}

