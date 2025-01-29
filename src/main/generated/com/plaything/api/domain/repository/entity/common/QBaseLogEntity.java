package com.plaything.api.domain.repository.entity.common;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseLogEntity is a Querydsl query type for BaseLogEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseLogEntity extends EntityPathBase<BaseLogEntity> {

  public static final QBaseLogEntity baseLogEntity = new QBaseLogEntity("baseLogEntity");
  private static final long serialVersionUID = 745014287L;
  public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt",
      java.time.LocalDateTime.class);

  public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt",
      java.time.LocalDateTime.class);

  public QBaseLogEntity(String variable) {
    super(BaseLogEntity.class, forVariable(variable));
  }

  public QBaseLogEntity(Path<? extends BaseLogEntity> path) {
    super(path.getType(), path.getMetadata());
  }

  public QBaseLogEntity(PathMetadata metadata) {
    super(BaseLogEntity.class, metadata);
  }

}

