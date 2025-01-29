package com.plaything.api.domain.repository.entity.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserCredentials is a Querydsl query type for UserCredentials
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserCredentials extends EntityPathBase<UserCredentials> {

  public static final QUserCredentials userCredentials = new QUserCredentials("userCredentials");
  private static final long serialVersionUID = 1804611690L;
  public final StringPath hashedPassword = createString("hashedPassword");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public QUserCredentials(String variable) {
    super(UserCredentials.class, forVariable(variable));
  }

  public QUserCredentials(Path<? extends UserCredentials> path) {
    super(path.getType(), path.getMetadata());
  }

  public QUserCredentials(PathMetadata metadata) {
    super(UserCredentials.class, metadata);
  }

}

