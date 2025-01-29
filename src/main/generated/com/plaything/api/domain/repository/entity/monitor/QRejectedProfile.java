package com.plaything.api.domain.repository.entity.monitor;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRejectedProfile is a Querydsl query type for RejectedProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRejectedProfile extends EntityPathBase<RejectedProfile> {

  public static final QRejectedProfile rejectedProfile = new QRejectedProfile("rejectedProfile");
  private static final long serialVersionUID = -680550757L;
  private static final PathInits INITS = PathInits.DIRECT2;
  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath introduction = createString("introduction");

  public final StringPath nickName = createString("nickName");

  public final NumberPath<Long> profileId = createNumber("profileId", Long.class);

  public final StringPath rejectedReason = createString("rejectedReason");

  public final com.plaything.api.domain.repository.entity.user.QUser user;

  public QRejectedProfile(String variable) {
    this(RejectedProfile.class, forVariable(variable), INITS);
  }

  public QRejectedProfile(Path<? extends RejectedProfile> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QRejectedProfile(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QRejectedProfile(PathMetadata metadata, PathInits inits) {
    this(RejectedProfile.class, metadata, inits);
  }

  public QRejectedProfile(Class<? extends RejectedProfile> type, PathMetadata metadata,
      PathInits inits) {
    super(type, metadata, inits);
    this.user =
        inits.isInitialized("user") ? new com.plaything.api.domain.repository.entity.user.QUser(
            forProperty("user"), inits.get("user")) : null;
  }

}

