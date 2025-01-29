package com.plaything.api.domain.repository.entity.user.profile;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProfileImage is a Querydsl query type for ProfileImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProfileImage extends EntityPathBase<ProfileImage> {

  public static final QProfileImage profileImage = new QProfileImage("profileImage");
  private static final long serialVersionUID = -569165122L;
  private static final PathInits INITS = PathInits.DIRECT2;
  public final StringPath fileName = createString("fileName");

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final BooleanPath isMainPhoto = createBoolean("isMainPhoto");

  public final QProfile profile;

  public final StringPath url = createString("url");

  public QProfileImage(String variable) {
    this(ProfileImage.class, forVariable(variable), INITS);
  }

  public QProfileImage(Path<? extends ProfileImage> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QProfileImage(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QProfileImage(PathMetadata metadata, PathInits inits) {
    this(ProfileImage.class, metadata, inits);
  }

  public QProfileImage(Class<? extends ProfileImage> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.profile = inits.isInitialized("profile") ? new QProfile(forProperty("profile")) : null;
  }

}

