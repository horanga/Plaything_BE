package com.plaything.api.domain.repository.entity.matching;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMatching is a Querydsl query type for Matching
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatching extends EntityPathBase<Matching> {

  public static final QMatching matching = new QMatching("matching");
  private static final long serialVersionUID = -1359449614L;
  public final com.plaything.api.domain.repository.entity.common.QBaseEntity _super = new com.plaything.api.domain.repository.entity.common.QBaseEntity(
      this);

  //inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  //inherited
  public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

  public final NumberPath<Long> receiverId = createNumber("receiverId", Long.class);

  public final NumberPath<Long> senderId = createNumber("senderId", Long.class);

  public QMatching(String variable) {
    super(Matching.class, forVariable(variable));
  }

  public QMatching(Path<? extends Matching> path) {
    super(path.getType(), path.getMetadata());
  }

  public QMatching(PathMetadata metadata) {
    super(Matching.class, metadata);
  }

}

