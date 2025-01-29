package com.plaything.api.domain.repository.entity.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

  public static final QUser user = new QUser("user");
  private static final long serialVersionUID = -4552910L;
  private static final PathInits INITS = PathInits.DIRECT2;
  public final com.plaything.api.domain.repository.entity.common.QBaseEntity _super = new com.plaything.api.domain.repository.entity.common.QBaseEntity(
      this);

  //inherited
  public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

  public final QUserCredentials credentials;

  public final StringPath fcmToken = createString("fcmToken");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath loginId = createString("loginId");

  //inherited
  public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

  public final com.plaything.api.domain.repository.entity.user.profile.QProfile profile;

  public final EnumPath<com.plaything.api.domain.profile.constants.Role> role = createEnum("role",
      com.plaything.api.domain.profile.constants.Role.class);

  public QUser(String variable) {
    this(User.class, forVariable(variable), INITS);
  }

  public QUser(Path<? extends User> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QUser(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QUser(PathMetadata metadata, PathInits inits) {
    this(User.class, metadata, inits);
  }

  public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.credentials =
        inits.isInitialized("credentials") ? new QUserCredentials(forProperty("credentials"))
            : null;
    this.profile = inits.isInitialized("profile")
        ? new com.plaything.api.domain.repository.entity.user.profile.QProfile(
        forProperty("profile")) : null;
  }

}

