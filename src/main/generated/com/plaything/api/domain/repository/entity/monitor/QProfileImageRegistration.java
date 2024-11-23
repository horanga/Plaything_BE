package com.plaything.api.domain.repository.entity.monitor;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProfileImageRegistration is a Querydsl query type for ProfileImageRegistration
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProfileImageRegistration extends EntityPathBase<ProfileImageRegistration> {

    private static final long serialVersionUID = 1115591099L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProfileImageRegistration profileImageRegistration = new QProfileImageRegistration("profileImageRegistration");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.plaything.api.domain.repository.entity.user.profile.QProfileImage profileImage;

    public QProfileImageRegistration(String variable) {
        this(ProfileImageRegistration.class, forVariable(variable), INITS);
    }

    public QProfileImageRegistration(Path<? extends ProfileImageRegistration> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProfileImageRegistration(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProfileImageRegistration(PathMetadata metadata, PathInits inits) {
        this(ProfileImageRegistration.class, metadata, inits);
    }

    public QProfileImageRegistration(Class<? extends ProfileImageRegistration> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.profileImage = inits.isInitialized("profileImage") ? new com.plaything.api.domain.repository.entity.user.profile.QProfileImage(forProperty("profileImage"), inits.get("profileImage")) : null;
    }

}

