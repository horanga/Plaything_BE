package com.plaything.api.domain.repository.entity.log;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QKeyLog is a Querydsl query type for KeyLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKeyLog extends EntityPathBase<KeyLog> {

  public static final QKeyLog keyLog = new QKeyLog("keyLog");
  private static final long serialVersionUID = 82486251L;
  private static final PathInits INITS = PathInits.DIRECT2;
  public final com.plaything.api.domain.repository.entity.common.QBaseLogEntity _super = new com.plaything.api.domain.repository.entity.common.QBaseLogEntity(
      this);

  //inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final com.plaything.api.domain.repository.entity.pay.QPointKey key;

  public final DateTimePath<java.time.LocalDateTime> KeyExpirationDate = createDateTime(
      "KeyExpirationDate", java.time.LocalDateTime.class);

  public final EnumPath<com.plaything.api.domain.key.constant.KeyLogStatus> keyLogStatus = createEnum(
      "keyLogStatus", com.plaything.api.domain.key.constant.KeyLogStatus.class);

  public final EnumPath<com.plaything.api.domain.key.constant.KeySource> keySource = createEnum(
      "keySource", com.plaything.api.domain.key.constant.KeySource.class);

  public final EnumPath<com.plaything.api.domain.key.constant.KeyType> keyType = createEnum(
      "keyType", com.plaything.api.domain.key.constant.KeyType.class);

  public final QKeyUsageLog keyUsageLog;

  //inherited
  public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

  public final com.plaything.api.domain.repository.entity.user.QUser user;

  public QKeyLog(String variable) {
    this(KeyLog.class, forVariable(variable), INITS);
  }

  public QKeyLog(Path<? extends KeyLog> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QKeyLog(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QKeyLog(PathMetadata metadata, PathInits inits) {
    this(KeyLog.class, metadata, inits);
  }

  public QKeyLog(Class<? extends KeyLog> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.key =
        inits.isInitialized("key") ? new com.plaything.api.domain.repository.entity.pay.QPointKey(
            forProperty("key"), inits.get("key")) : null;
    this.keyUsageLog =
        inits.isInitialized("keyUsageLog") ? new QKeyUsageLog(forProperty("keyUsageLog")) : null;
    this.user =
        inits.isInitialized("user") ? new com.plaything.api.domain.repository.entity.user.QUser(
            forProperty("user"), inits.get("user")) : null;
  }

}

