package com.plaything.api.domain.repository.entity.log;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdViewLog is a Querydsl query type for AdViewLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdViewLog extends EntityPathBase<AdViewLog> {

  public static final QAdViewLog adViewLog = new QAdViewLog("adViewLog");
  private static final long serialVersionUID = -397819018L;
  private static final PathInits INITS = PathInits.DIRECT2;
  public final com.plaything.api.domain.repository.entity.common.QBaseLogEntity _super = new com.plaything.api.domain.repository.entity.common.QBaseLogEntity(
      this);

  public final StringPath adType = createString("adType");

  //inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  //inherited
  public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

  public final com.plaything.api.domain.repository.entity.user.QUser user;

  public final NumberPath<Integer> viewDuration = createNumber("viewDuration", Integer.class);

  public QAdViewLog(String variable) {
    this(AdViewLog.class, forVariable(variable), INITS);
  }

  public QAdViewLog(Path<? extends AdViewLog> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QAdViewLog(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QAdViewLog(PathMetadata metadata, PathInits inits) {
    this(AdViewLog.class, metadata, inits);
  }

  public QAdViewLog(Class<? extends AdViewLog> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user") ? new com.plaything.api.domain.repository.entity.user.QUser(
            forProperty("user"), inits.get("user")) : null;
  }

}

