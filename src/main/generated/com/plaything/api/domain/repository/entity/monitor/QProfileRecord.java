package com.plaything.api.domain.repository.entity.monitor;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProfileRecord is a Querydsl query type for ProfileRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProfileRecord extends EntityPathBase<ProfileRecord> {

    private static final long serialVersionUID = 62967466L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProfileRecord profileRecord = new QProfileRecord("profileRecord");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath introduction = createString("introduction");

    public final StringPath nickName = createString("nickName");

    public final NumberPath<Long> profileId = createNumber("profileId", Long.class);

    public final EnumPath<com.plaything.api.domain.user.constants.ProfileStatus> profileStatus = createEnum("profileStatus", com.plaything.api.domain.user.constants.ProfileStatus.class);

    public final com.plaything.api.domain.repository.entity.user.QUser user;

    public QProfileRecord(String variable) {
        this(ProfileRecord.class, forVariable(variable), INITS);
    }

    public QProfileRecord(Path<? extends ProfileRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProfileRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProfileRecord(PathMetadata metadata, PathInits inits) {
        this(ProfileRecord.class, metadata, inits);
    }

    public QProfileRecord(Class<? extends ProfileRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.plaything.api.domain.repository.entity.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

